package iot.sbc2ha;

import iot.sbc2ha.boot.BootDisplay;
import iot.sbc2ha.boot.FakeBootDisplay;
import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.HardwareConfig;
import iot.sbc2ha.config.ProfileConfig;
import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.config.ValidationException;
import iot.sbc2ha.device.ClicksConfig;
import iot.sbc2ha.device.DeviceConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.InputDevice;
import iot.sbc2ha.device.LightDevice;
import iot.sbc2ha.device.OutputDevice;
import iot.sbc2ha.device.SwitchDevice;
import iot.sbc2ha.hardware.GpioChannel;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.HardwareMappingException;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.Mcp23017Channel;
import iot.sbc2ha.hardware.OledChannel;
import iot.sbc2ha.hardware.PhysicalChannel;
import iot.sbc2ha.hardware.profile.HardwareProfile;
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
import iot.sbc2ha.mqtt.MqttBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public static void main(String[] args) {
        Lifecycle lifecycle = new Lifecycle(new FakeBootDisplay());
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
            Throwable cause = e.getCause();
            while (cause != null) {
                log.error("  Cause: {}", cause.getMessage());
                cause = cause.getCause();
            }
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
        start(config, new Lifecycle(new FakeBootDisplay()));
    }

    /**
     * Internal start that owns the lifecycle manager.
     */
    @SuppressWarnings("Resource") // ScheduledExecutorService managed by shutdown hook
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
        var sharedScheduler = hardwareReady(config, engine, lifecycle);

        lifecycle.transition(LifecycleState.HARDWARE_MINIMAL_READY);
        lifecycle.transition(LifecycleState.OFFLINE_READY);
        log.info("sbc2ha ready (OFFLINE_READY).");

        // Optional MQTT layer — never blocks OFFLINE_READY, managed by shutdown hook
        final MqttBroker mqttBrokerRef;
        if (config.mqtt() != null && config.mqtt().isEnabled()) {
            mqttBrokerRef = new MqttBroker(config.mqtt(), lifecycle);
            mqttBrokerRef.connect();
        } else {
            mqttBrokerRef = null;
        }

        // TODO: enter main event loop (WebSocket, etc.)

        // Shutdown hook: clean up shared scheduler and MQTT broker
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (mqttBrokerRef != null) {
                    mqttBrokerRef.shutdown();
                }
            } catch (Exception e) {
                log.warn("MQTT broker close failed: {}", e.getMessage());
            }
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
     * @param lifecycle the lifecycle manager (for OLED display swap)
     * @return the shared scheduler for all click detectors (caller owns shutdown)
     */
    private static ScheduledExecutorService hardwareReady(Sbc2haConfig config,
                                                          ActionEngine engine,
                                                          Lifecycle lifecycle) {
        HardwareConfig hwConfig = config.hardware();
        if (hwConfig == null || hwConfig.profiles().isEmpty()) {
            log.info("No hardware profiles configured — skipping hardware wiring");
            return null;
        }

        // Build alias → HardwareProfile map and load profiles into registry
        Map<String, HardwareProfile> aliasProfileMap = new LinkedHashMap<>();
        ProfileRegistry registry = new ProfileRegistry();
        for (var entry : hwConfig.profiles().entrySet()) {
            String alias = entry.getKey();
            ProfileConfig pconf = entry.getValue();
            try {
                // Profile type ID uses dots: "namespace.name" (e.g. "boneio.input-v0.3").
                // Classpath resource is "hardware-profiles/namespace/name.yaml" — only the first dot is a path separator.
                String type = pconf.type();
                int firstDot = type.indexOf('.');
                String resourcePath;
                if (firstDot >= 0) {
                    resourcePath = "hardware-profiles/" + type.substring(0, firstDot) + "/" + type.substring(firstDot + 1) + ".yaml";
                } else {
                    resourcePath = "hardware-profiles/" + type + ".yaml";
                }
                HardwareProfile profile = registry.loadFromClasspath(resourcePath);
                aliasProfileMap.put(alias, profile);
                log.info("Loaded hardware profile '{}' as alias '{}' from {}", type, alias, resourcePath);
            } catch (Exception e) {
                log.warn("Failed to load profile '{}' as alias '{}': {}", pconf.type(), alias, e.getMessage());
                continue;
            }
        }

        log.info("Registry state: {} profiles registered", registry.profileCount());

        // Expand all profiles and log their contents
        try {
            Map<String, HardwareModel> aliasModelMap = new LinkedHashMap<>();
            for (var entry : aliasProfileMap.entrySet()) {
                String alias = entry.getKey();
                HardwareProfile profile = entry.getValue();
                // Register in registry by profile ID for expand()
                registry.register(profile);
                HardwareModel expanded = registry.expand(profile.id(), null, null);
                aliasModelMap.put(alias, expanded);
                log.info("Profile alias '{}' (type '{}') → {} chips, {} channels, {} mappings",
                        alias, profile.id(), expanded.chips().size(),
                        expanded.channelCount(), expanded.mappingCount());
            }

            // Validate: every device's input/output reference resolves to a profile mapping
            validateDeviceReferences(config.registry(), aliasProfileMap);
            log.info("All device hardware references validated");

            // Create real hardware factory
            InputOutputFactory factory = DiozeroInputOutputFactory.INSTANCE;

            // Discover OLED channel from profiles and swap display
            OledChannel oledChannel = findOledChannel(aliasProfileMap);
            HardwareModel oledModel = null;
            if (oledChannel != null) {
                // Find the model that contains this OLED channel
                for (var model : aliasModelMap.values()) {
                    for (var ch : model.channels()) {
                        if (ch == oledChannel) {
                            oledModel = model;
                            break;
                        }
                    }
                    if (oledModel != null) break;
                }
                BootDisplay oledDisplay = ((DiozeroInputOutputFactory) factory)
                        .createOledDisplay(oledChannel, oledModel);
                if (oledDisplay != null) {
                    lifecycle.setDisplay(oledDisplay);
                    log.info("OLED display swapped from FakeBootDisplay to OledBootDisplay");
                }
            }

            // Single shared scheduler for all click detectors (50 inputs → 1 thread instead of 50)
            var sharedScheduler = Executors.newScheduledThreadPool(0);

            // Wire each device in the registry
            DeviceRegistry registry2 = config.registry();
            for (var dev : registry2.all()) {
                try {
                    PhysicalChannel channel = resolveDeviceHardware(dev, aliasProfileMap);
                    String channelDesc = describeChannel(channel);

                    // Find the model that contains this channel
                    HardwareModel devModel = findModelForChannel(channel, aliasModelMap);

                    switch (dev) {
                        case SwitchDevice switchDev -> {
                            // Switch: input adapter → debouncer → click detector
                            InputAdapter inputAdapter;
                            if (channel instanceof GpioChannel gpioCh) {
                                inputAdapter = factory.createInput(gpioCh.pinLabel());
                            } else {
                                // MCP23017 input — use factory with model for chip resolution
                                inputAdapter = ((DiozeroInputOutputFactory) factory)
                                        .createInput(channel, devModel);
                            }
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
                                log.info("Wired switch '{}' ({}) → SimpleDebouncer[{}ms] → SimpleClickDetector[{}]",
                                        dev.id(), channelDesc, clicksConfig.debounceMs(), clicksConfig);
                            } else {
                                log.warn("Switch '{}' has no runtime — cannot wire click detector", dev.id());
                            }
                        }
                        case OutputDevice _ -> {
                            // Output: physical output adapter bound to OutputRuntime
                            OutputAdapter outAdapter = ((DiozeroInputOutputFactory) factory)
                                    .createOutput(channel, devModel);
                            OutputRuntime outRuntime = (OutputRuntime) engine.getTarget(dev.id());
                            if (outRuntime != null) {
                                outRuntime.bindAdapter(outAdapter);
                            }
                            log.info("Wired output '{}' ({})", dev.id(), channelDesc);
                        }
                        case LightDevice _ -> {
                            // Light: physical output adapter bound to LightRuntime
                            OutputAdapter lightAdapter = ((DiozeroInputOutputFactory) factory)
                                    .createOutput(channel, devModel);
                            LightRuntime lightRuntime = (LightRuntime) engine.getTarget(dev.id());
                            if (lightRuntime != null) {
                                lightRuntime.bindAdapter(lightAdapter);
                            }
                            log.info("Wired light '{}' ({})", dev.id(), channelDesc);
                        }
                        case InputDevice _ -> {
                            // Input: physical input adapter bound to InputRuntime
                            InputAdapter inAdapter;
                            if (channel instanceof GpioChannel gpioCh) {
                                inAdapter = factory.createInput(gpioCh.pinLabel());
                            } else {
                                inAdapter = ((DiozeroInputOutputFactory) factory)
                                        .createInput(channel, devModel);
                            }
                            InputRuntime inRuntime = (InputRuntime) engine.getTarget(dev.id());
                            if (inRuntime != null) {
                                inRuntime.bindAdapter(inAdapter);
                            }
                            log.info("Wired input '{}' ({})", dev.id(), channelDesc);
                        }
                        default -> log.warn("Unknown device type '{}' ({})", dev.id(), dev.getClass().getSimpleName());
                    }
                } catch (Exception e) {
                    log.error("Failed to wire device '{}': {}", dev.id(), e.getMessage());
                }
            }

            log.info("Hardware wiring complete");
            return sharedScheduler;

        } catch (Exception e) {
            log.warn("Hardware model expansion failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Resolve a device's hardware channel by parsing its input/output reference.
     * <p>
     * Format: {@code alias.inputN} → look up alias in profile map → find mapping with
     * logical_id matching {@code input_N} (or {@code output_N}).
     *
     * @param dev the device config
     * @param aliasProfileMap alias → loaded profile
     * @return the physical channel for this device
     * @throws HardwareMappingException if the reference cannot be resolved
     */
    private static PhysicalChannel resolveDeviceHardware(DeviceConfig dev,
                                                          Map<String, HardwareProfile> aliasProfileMap) {
        String ref = dev.input() != null ? dev.input() : dev.output();
        if (ref == null || ref.isBlank()) {
            throw new HardwareMappingException("Device '" + dev.id() + "' has no input/output reference");
        }

        String[] parts = ref.split("\\.", 2);
        if (parts.length != 2) {
            throw new HardwareMappingException(
                    "Invalid hardware reference '" + ref + "' for device '" + dev.id()
                    + "' (expected 'alias.inputN')");
        }

        String alias = parts[0];
        String inputRef = parts[1];  // e.g. "input1"

        HardwareProfile profile = aliasProfileMap.get(alias);
        if (profile == null) {
            throw new HardwareMappingException(
                    "Unknown hardware profile alias '" + alias + "' for device '" + dev.id()
                    + "' (available: " + aliasProfileMap.keySet() + ")");
        }

        // Find matching mapping in the profile: input1 → input_1
        HardwareMapping mapping = findMappingByInputRef(profile, inputRef);
        if (mapping == null) {
            throw new HardwareMappingException(
                    "No mapping for '" + inputRef + "' in profile alias '" + alias + "'"
                    + " (device '" + dev.id() + "')");
        }

        return mapping.physical();
    }

    /**
     * Find a profile mapping by input reference (e.g. "input1" → mapping with logical_id "input_1").
     * <p>
     * Tries exact match first, then appends underscore before trailing digits.
     */
    private static HardwareMapping findMappingByInputRef(HardwareProfile profile, String inputRef) {
        // Try exact match first
        for (HardwareMapping m : profile.mappings()) {
            if (inputRef.equals(m.logicalId())) {
                return m;
            }
        }

        // Try adding underscore before trailing digits: input1 → input_1
        int lastDigitEnd = inputRef.length();
        while (lastDigitEnd > 0 && Character.isDigit(inputRef.charAt(lastDigitEnd - 1))) {
            lastDigitEnd--;
        }
        if (lastDigitEnd > 0 && lastDigitEnd < inputRef.length()) {
            String variant = inputRef.substring(0, lastDigitEnd) + "_" + inputRef.substring(lastDigitEnd);
            for (HardwareMapping m : profile.mappings()) {
                if (variant.equals(m.logicalId())) {
                    return m;
                }
            }
        }

        return null;
    }

    /**
     * Validate that every device's input/output reference resolves to a profile mapping.
     */
    private static void validateDeviceReferences(DeviceRegistry registry,
                                                  Map<String, HardwareProfile> aliasProfileMap) {
        for (DeviceConfig dev : registry.all()) {
            try {
                resolveDeviceHardware(dev, aliasProfileMap);
            } catch (HardwareMappingException e) {
                throw new HardwareMappingException(e.getMessage());
            }
        }
    }

    /**
     * Search all loaded profiles for an OLED channel.
     * Returns the first OledChannel found, or {@code null} if none.
     */
    private static OledChannel findOledChannel(Map<String, HardwareProfile> aliasProfileMap) {
        for (HardwareProfile profile : aliasProfileMap.values()) {
            for (PhysicalChannel ch : profile.channels()) {
                if (ch instanceof OledChannel) {
                    return (OledChannel) ch;
                }
            }
        }
        return null;
    }

    /**
     * Describe a physical channel for logging.
     */
    private static String describeChannel(PhysicalChannel ch) {
        return switch (ch) {
            case GpioChannel gc -> "pin=" + gc.pin();
            case Mcp23017Channel mc -> "bus=" + mc.bus() + ", pin=" + mc.pin();
            case OledChannel oc -> "bus=" + oc.bus();
            default -> ch.toString();
        };
    }

    /**
     * Find the HardwareModel that contains the given channel.
     */
    private static HardwareModel findModelForChannel(PhysicalChannel channel,
                                                      Map<String, HardwareModel> aliasModelMap) {
        for (var model : aliasModelMap.values()) {
            if (model.channels().contains(channel)) {
                return model;
            }
        }
        return null;
    }
}
