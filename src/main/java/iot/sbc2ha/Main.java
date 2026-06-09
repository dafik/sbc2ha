package iot.sbc2ha;

import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.runtime.ActionEngine;
import iot.sbc2ha.runtime.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_STATE_FILE = "state.json";

    public static void main(String[] args) {
        log.info("sbc2ha starting...");

        Sbc2haConfig config = null;
        if (args.length > 0) {
            config = ConfigLoader.load(args[0]);
            log.info("loaded config: {}", config);
        } else {
            log.warn("no config file specified (args[0])");
            System.exit(1);
        }

        start(config);
    }

    /**
     * Wire the loaded config into the runtime engine and enter the main loop.
     *
     * @param config the validated application configuration
     */
    public static void start(Sbc2haConfig config) {
        log.info("Initializing runtime engine...");

        String stateFilePath = System.getProperty("sbc2ha.state", DEFAULT_STATE_FILE);
        StateService stateService = new StateService(Paths.get(stateFilePath));
        @SuppressWarnings("unused")
        ActionEngine engine = new ActionEngine(config.registry(), stateService);

        log.info("sbc2ha ready.");

        // TODO: enter main event loop (hardware input polling, MQTT, etc.)
    }
}
