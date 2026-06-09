package iot.sbc2ha;

import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.Sbc2haConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("sbc2ha starting...");

        Sbc2haConfig config;
        if (args.length > 0) {
            config = ConfigLoader.load(args[0]);
            log.info("loaded config: {}", config);
        } else {
            log.warn("no config file specified (args[0])");
            System.exit(1);
        }

        log.info("sbc2ha ready.");
    }
}
