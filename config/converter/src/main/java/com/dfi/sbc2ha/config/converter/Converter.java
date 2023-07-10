package com.dfi.sbc2ha.config.converter;


import com.dfi.sbc2ha.config.boneio.definition.BoneIoConfig;
import com.dfi.sbc2ha.config.converter.config.ConfigHelper;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class Converter {


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        try {

            String configFile = getConfigFile(args);
            String vendor = getVendor(args);
            AppConfig appConfig = convertFile(configFile, vendor);

            saveConfig(appConfig, configFile);


        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        long millis = System.currentTimeMillis() - startTime;
        String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        Logger.info("converted in {}", took);

    }

    private static void saveConfig(AppConfig appConfig, String configFile) {
        ConfigHelper.saveCacheYaml(configFile, appConfig, "-sbc2ha");
    }

    public static AppConfig convertFile(String fileLocation, String vendor) {
        BoneIoConfig boneIoConfig = getAppConfig(fileLocation);
        if (Objects.equals(vendor, "boneio")) {
            return BoneIoConverter.convertBoneIo(boneIoConfig);
        } else if (vendor.equals("rpi")) {
            return BoneIoConverter.convert(boneIoConfig, vendor, "nohat", "nohat");
        } else {
            throw new IllegalArgumentException("unknown vendor " + vendor);
        }
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

    static String getVendor(String[] args) throws IllegalArgumentException {
        if (args.length > 1) {
            return args[1];
        }
        return "boneio";
    }
}


