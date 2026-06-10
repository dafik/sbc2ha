package iot.sbc2ha;

import iot.sbc2ha.boot.FakeBootDisplay;
import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.boot.OledBootDisplay;
import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.HardwareConfig;
import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.config.ValidationException;
import iot.sbc2ha.device.ClicksConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.InputDevice;
import iot.sbc2ha.device.LightDevice;
import iot.sbc2ha.device.OutputDevice;
import iot.sbc2ha.device.SwitchDevice;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.PhysicalChannel;
import iot.sbc2ha.hardware.profile.ProfileRegistry;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.InputAdapter.InputListener;
import iot.sbc2ha.hardware.io.InputOutputFactory;
import iot.sbc2ha.hardware.io.OutputAdapter;
import iot.sbc2ha.hardware.io.diozero.DiozeroInputOutputFactory;
import iot.sbc2ha.input.ClickDetector;
import iot.sbc2ha.input.SimpleClickDetector;
import iot.sbc2ha.input.SimpleDebouncer;
import iot.sbc2ha.runtime.ActionEngine;
import iot.sbc2ha.runtime.InputRuntime;
import iot.sbc2ha.runtime.LightRuntime;
import iot.sbc2ha.runtime.OutputRuntime;
import iot.sbc2ha.runtime.StateService;
import iot.sbc2ha.runtime.SwitchRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * sbc2ha — single-board computer to Home Assistant gateway.
 *
 * <h3>Boot sequence</h3>
 * <ol>
 *   <li>BOOTING → CONFIG_LOADED: load and validate YAML config</li>
 *   <li>STATE_RESTORED: restore actuator state from persistent store</li>
 *   <li>HARDWARE_MINIMAL_READY: create hardware adapters and wire runtime</li>
 *   <li>OFFLINE_READY: all cores operational (MQTT/HA optional layers may be disabled)</li>
 * </ol>
 *
 * <h3>Filesystem layout</h3>
 * <ul>
 *   <li>Config: mandatory CLI arg (args[0])</li>
 *   <li>State: -Dsbc2ha.state (default /var/lib/sbc2ha/state.json)</li>
 *   <li>Logs: logback.xml, configurable via -Dsbc2ha.logdir</li>
 * </ul>
 *
 * @see <a href="https://github.com/zw/sbc2ha/tree/next/ops">ops/ deployment docs</a>
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    /** Default state file path — see ops/filesystem-layout.md */
    static final String DEFAULT_STATE_FILE = "/var/lib/sbc2ha/state.json";
    /** Default log directory — see ops/filesystem-layout.md */
    static final String DEFAULT_LOG_DIR = "/var/log/sbc2ha";

    /**
     * Create a BootDisplay, trying real OLED first and falling back to log-only.
     */
    private static iot.sbc2ha.boot.BootDisplay createBootDisplay() {
        try {
            return new OledBootDisplay();
        } catch (Exception e) {
            log.info("OLED display not available, using log-only BootDisplay: {}", e.getMessage());
            return new FakeBootDisplay();
        }
    }

    public static void main(String[] args) {
        Lifecycle lifecycle = new Lifecycle(createBootDisplay());
        lifecycle.transition(LifecycleState.BOOTING);

        Sbc2haConfig config = null;
        try {
            if (args.length > 0) {
                config = ConfigLoader.load(args[0]);
                lifecycle.transition(LifecycleState.CONFIG_LOADED);
                log.info("loaded config: {}", config);
            } else {
                log.warn("no config file specified (args[0])");
                lifecycle.transition(LifecycleState.CONFIG_ERROR);
                lifecycle.shutdown();
                System.exit(1);
            }
        } catch (ValidationException e) {
            log.error("Configuration error: {}", e.getMessage());
            lifecycle.transition(LifecycleState.CONFIG_ERROR);
            lifecycle.shutdown();
            System.exit(1);
        }

        start(config, lifecycle);
    }

    /**
     * Wire the loaded config into the runtime engine and enter the main loop.
     *
     * @param config the validated application configuration
     */
    public static void start(Sbc2haConfig config) {
        start(config, new Lifecycle(createBootDisplay()));
    }

    /**
     * Internal start that owns the lifecycle manager.
     */
    @SuppressWarnings("unused")
    private static void start(Sbc2haConfig config, Lifecycle lifecycle) {
        String stateFilePath = System.getProperty("sbc2ha.state", DEFAULT_STATE_FILE);
        StateService stateService = new StateService(Paths.get(stateFilePath));
        lifecycle.transition(LifecycleState.STATE_RESTORED);

        // Build ActionEngine (wires switchs → targets, restores persisted state)
        ActionEngine engine = new ActionEngine(config.registry(), stateService);
        log.info("ActionEngine built: {} switchs, {} targets",
                engine.switchs().size(), engine.targets().size());

        // Wire real hardware: resolve physical channels, create adapters, bind to runtimes
        // Note: scheduler is long-lived (app lifetime), not managed here
        @SuppressWarnings("resource")
        var sharedScheduler = hardwareReady(config, engine);

        lifecycle.transition(LifecycleState.HARDWARE_MINIMAL_READY);
        lifecycle.transition(LifecycleState.OFFLINE_READY);
        log.info("sbc2ha ready (OFFLINE_READY).");

        // TODO: enter main event loop (MQTT, WebSocket, etc.)

        // Shutdown hook: clean up shared scheduler
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (sharedScheduler != null) {
                sharedScheduler.shutdownNow();
                log.info("Shared scheduler shut down.");
            }
        }));

        lifecycle.shutdown();
    }

    /**
     * Resolve hardware profiles and bind adapters to runtimes.
     *
     * @param config the loaded application config
     * @param engine the action engine with switch/target runtimes
     * @return the shared scheduler for all click detectors (caller owns shutdown)
     */
    private static ScheduledExecutorService hardwareReady(Sbc2haConfig config, ActionEngine engine) {
        HardwareConfig hwConfig = config.hardware();
        if (hwConfig == null || hwConfig.profiles().isEmpty()) {
            log.info("No hardware profiles configured — skipping hardware wiring");
            return null;
        }

        // Load hardware profiles into registry
        ProfileRegistry registry = new ProfileRegistry();
        for (var entry : hwConfig.profiles().entrySet()) {
            try {
                // Profile name from config key; actual profile ID comes from YAML
                registry.loadFromClasspath("hardware-profiles/" + entry.getValue().type() + ".yaml");
                log.debug("Loaded hardware profile: {}", entry.getValue().type());
            } catch (Exception e) {
                log.warn("Failed to load profile '{}': {}", entry.getValue().type(), e.getMessage());
            }
        }

        // Expand profiles into HardwareModel
        // For now, expand all profiles into a single model
        // In a real scenario, we'd merge based on bus references
        HardwareModel model = null;
        try {
            // Find the board profile and expand
            for (var profileId : registry.registeredProfileNames()) {
                model = registry.expand(profileId, null, null);
                log.info("Expanded hardware profile '{}' → {} channels, {} mappings",
                        profileId, model.channelCount(), model.mappingCount());
                break; // Use first (board) profile for now
            }

            if (model == null) {
                log.warn("No hardware profiles could be expanded");
                return null;
            }

            // Validate all devices have mappings
            model.validate(config.registry().allIds());
            log.info("Hardware model validated: {} devices mapped", model.mappingCount());

        } catch (Exception e) {
            log.warn("Hardware model expansion failed: {}", e.getMessage());
            return null;
        }

        // Create real hardware factory
        InputOutputFactory factory = DiozeroInputOutputFactory.INSTANCE;

        // Single shared scheduler for all click detectors (50 inputs → 1 thread instead of 50)
        var sharedScheduler = Executors.newScheduledThreadPool(0);

        // Wire each device in the registry
        DeviceRegistry registry2 = config.registry();
        for (var dev : registry2.all()) {
            try {
                PhysicalChannel channel = model.resolve(dev.id());
                String location = channel.location();

                switch (dev) {
                    case SwitchDevice switchDev -> {
                        // Switch: input adapter → debouncer → click detector
                        InputAdapter inputAdapter = factory.createInput(location);
                        SwitchRuntime switchRuntime = (SwitchRuntime) engine.getTarget(dev.id());
                        if (switchRuntime != null) {
                            ClicksConfig clicksConfig = switchDev.clicks() != null
                                    ? switchDev.clicks()
                                    : new ClicksConfig(); // default: click only
                            ClickDetector detector = new SimpleClickDetector(
                                    sharedScheduler,
                                    clicksConfig,
                                    event -> engine.dispatchEvent(switchRuntime, event));
                            // Wire debouncer between adapter and click detector
                            SimpleDebouncer debouncer = new SimpleDebouncer(
                                    clicksConfig.debounceMs(),
                                    (val, _ts) -> {
                                        if (val) detector.onRelease(_ts);
                                        else detector.onPress(_ts);
                                    });
                            InputListener listener = new InputListener() {
                                @Override public void pressed(long t) {
                                    debouncer.accept(true, t * 1_000_000L);
                                }
                                @Override public void released(long t) {
                                    debouncer.accept(false, t * 1_000_000L);
                                }
                            };
                            inputAdapter.addInputListener(listener);
                            log.info("Wired switch '{}' (input={}) → SimpleDebouncer[{}ms] → SimpleClickDetector[{}]",
                                    dev.id(), location, clicksConfig.debounceMs(), clicksConfig);
                        } else {
                            log.warn("Switch '{}' has no runtime — cannot wire click detector", dev.id());
                        }
                    }
                    case OutputDevice _ -> {
                        // Output: physical output adapter bound to OutputRuntime
                        OutputAdapter outAdapter = factory.createOutput(location);
                        OutputRuntime outRuntime = (OutputRuntime) engine.getTarget(dev.id());
                        if (outRuntime != null) {
                            outRuntime.bindAdapter(outAdapter);
                        }
                        log.info("Wired output '{}' (location={})", dev.id(), location);
                    }
                    case LightDevice _ -> {
                        // Light: physical output adapter bound to LightRuntime
                        OutputAdapter lightAdapter = factory.createOutput(location);
                        LightRuntime lightRuntime = (LightRuntime) engine.getTarget(dev.id());
                        if (lightRuntime != null) {
                            lightRuntime.bindAdapter(lightAdapter);
                        }
                        log.info("Wired light '{}' (location={})", dev.id(), location);
                    }
                    case InputDevice _ -> {
                        // Input: physical input adapter bound to InputRuntime
                        InputAdapter inAdapter = factory.createInput(location);
                        InputRuntime inRuntime = (InputRuntime) engine.getTarget(dev.id());
                        if (inRuntime != null) {
                            inRuntime.bindAdapter(inAdapter);
                        }
                        log.info("Wired input '{}' (location={})", dev.id(), location);
                    }
                    default -> log.warn("Unknown device type '{}' ({})", dev.id(), dev.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("Failed to wire device '{}': {}", dev.id(), e.getMessage());
            }
        }

        log.info("Hardware wiring complete");
        return sharedScheduler;
    }
}
