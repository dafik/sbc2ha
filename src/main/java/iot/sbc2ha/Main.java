package iot.sbc2ha;

import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.runtime.ActionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

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

        @SuppressWarnings("unused")
        ActionEngine engine = new ActionEngine(config.registry());

        log.info("sbc2ha ready.");

        // TODO: enter main event loop (hardware input polling, MQTT, etc.)
    }
}
