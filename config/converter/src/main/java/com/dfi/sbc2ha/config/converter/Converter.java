package com.dfi.sbc2ha.config.converter;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.dfi.sbc2ha.config.boneio.definition.BoneIoConfig;
import com.dfi.sbc2ha.config.converter.config.ConfigHelper;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Converter {


    public static void main(String[] args) throws Exception {
        CommandLineOpts cmd = new CommandLineOpts();

        JCommander jCommander = JCommander.newBuilder()
                .addObject(cmd)
                .programName("java -jar converter-0.0.1.jar")
                .build();


        if (args.length == 0) {
            jCommander.usage();
        } else {
            try {
                jCommander.parse(args);
            } catch (ParameterException e) {
                System.err.println(e.getMessage());
            }
            cmdLineConvert(cmd);
        }
    }

    public static void cmdLineConvert(CommandLineOpts cmd) throws Exception {

        long startTime = System.currentTimeMillis();
        try {

            String configFile = cmd.configFile;
            AppConfig appConfig = convertFile(configFile, cmd.vendor, cmd.inputBoard, cmd.outputBoard);

            saveConfig(appConfig, configFile);


        } catch (IllegalArgumentException e) {
            throw new Exception(e);
        }

        long millis = System.currentTimeMillis() - startTime;
        String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        log.info("converted in {}", took);

    }

    private static void saveConfig(AppConfig appConfig, String configFile) {
        ConfigHelper.saveCacheYaml(configFile, appConfig, "-sbc2ha");
    }

    public static AppConfig convertFile(String fileLocation, String vendor, String inputBoard, String outputBoard) {
        BoneIoConfig boneIoConfig = getAppConfig(fileLocation);
        if (Objects.equals(vendor, "boneio")) {
            return BoneIoConverter.convertBoneIo(boneIoConfig, inputBoard, outputBoard);
        } else if (vendor.equals("rpi")) {
            return BoneIoConverter.convertRpi(boneIoConfig, inputBoard, outputBoard);
        } else {
            throw new IllegalArgumentException("unknown vendor " + vendor);
        }
    }


    static BoneIoConfig getAppConfig(String configFile) throws IllegalArgumentException {
        try {
            BoneIoConfig boneIoConfig = ConfigHelper.loadConfig(configFile);

            return boneIoConfig;
        } catch (URISyntaxException | IOException | RuntimeException e) {
            log.error("load config failed {}", e.getMessage());
            //System.exit(1);
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


