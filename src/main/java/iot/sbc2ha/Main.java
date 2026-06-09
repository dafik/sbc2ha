package iot.sbc2ha;

import iot.sbc2ha.boot.FakeBootDisplay;
import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.config.ValidationException;
import iot.sbc2ha.runtime.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_STATE_FILE = "state.json";

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
     *
     * @param config the validated application configuration (used by future hardware adapters)
     */
    @SuppressWarnings("unused")
    private static void start(Sbc2haConfig config, Lifecycle lifecycle) {
        String stateFilePath = System.getProperty("sbc2ha.state", DEFAULT_STATE_FILE);
        @SuppressWarnings("unused")
        StateService stateService = new StateService(Paths.get(stateFilePath));
        lifecycle.transition(LifecycleState.STATE_RESTORED);

        // No real hardware yet; skip HARDWARE_MINIMAL_READY.
        // When hardware adapters exist this transition would occur here.

        lifecycle.transition(LifecycleState.OFFLINE_READY);
        log.info("sbc2ha ready (OFFLINE_READY).");

        // TODO: enter main event loop (hardware input polling, MQTT, etc.)

        lifecycle.shutdown();
    }
}
