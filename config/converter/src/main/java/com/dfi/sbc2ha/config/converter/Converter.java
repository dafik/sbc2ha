package com.dfi.sbc2ha.config.converter;


import com.dfi.sbc2ha.config.boneio.definition.BoneIoConfig;
import com.dfi.sbc2ha.config.converter.config.ConfigHelper;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;


public class Converter {


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        try {

            String configFile = getConfigFile(args);
            AppConfig appConfig = convertFile(configFile);

            saveConfig(appConfig,configFile);


        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        long millis = System.currentTimeMillis() - startTime;
        String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        Logger.info("converted in {}", took);

    }

    private static void saveConfig(AppConfig appConfig, String configFile) {
        ConfigHelper.saveCacheYaml(configFile,appConfig,"-sbc2ha");
    }

    public static AppConfig convertFile(String fileLocation) {
        BoneIoConfig boneIoConfig = getAppConfig(fileLocation);
        return BoneIoConverter.convert(boneIoConfig);
    }


    static BoneIoConfig getAppConfig(String configFile) throws IllegalArgumentException {
        try {
            BoneIoConfig boneIoConfig = ConfigHelper.loadConfig(configFile);

            return boneIoConfig;
        } catch (URISyntaxException | IOException | RuntimeException e) {
            Logger.error("load config failed {}", e.getMessage());
            System.exit(1);
            throw new IllegalArgumentException(e);
        }
    }

    static String getConfigFile(String[] args) throws IllegalArgumentException {
        if (args.length > 0) {
            return args[0];
        }
        throw new IllegalArgumentException("no config file provided as first argument");
    }
}


