package com.dfi.sbc2ha;

import com.dfi.sbc2ha.config.ConfigHelper;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.LoggerConfig;
import com.dfi.sbc2ha.exception.MissingConfigException;
import com.dfi.sbc2ha.log.ReconfigurableLoggingProvider;
import com.dfi.sbc2ha.manager.Manager;
import com.diozero.api.RuntimeInterruptedException;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class App {

    public static final String DIOZERO_DEVICEFACTORY = "diozero.devicefactory";
    private static final String DEFAULT_DEVICEFACTORY_CLASS = "com.diozero.internal.provider.builtin.DefaultDeviceFactory";


    public static void main(String[] args) {


        long startTime = System.currentTimeMillis();
        log.info("App starting {}", Version.VERSION);

        try {
            configureDeviceFactory();
            System.setProperty("useDiozeroSerial", "1");

            String configFile = getConfigFile(args);
            AppConfig appConfig = getAppConfig(startTime, configFile);

            configureLogging(appConfig.getLogger());

            Manager manager = new Manager(appConfig);
            Diozero.registerForShutdown(manager);

        } catch (MissingConfigException e) {
            throw new RuntimeException(e);
        }

        long millis = System.currentTimeMillis() - startTime;
        String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        log.info("App started in {}", took);

        runLoop();
    }

    private static void configureDeviceFactory() {
        String deviceFactoryClass = System.getProperty(DIOZERO_DEVICEFACTORY);
        if (deviceFactoryClass == null) {
            System.setProperty(DIOZERO_DEVICEFACTORY, DEFAULT_DEVICEFACTORY_CLASS);
        }
    }

    private static void runLoop() {
        while (true) {
            try {
                SleepUtil.sleepSeconds(100);
            } catch (RuntimeInterruptedException e) {
                System.exit(0);
            }

        }
    }

    private static AppConfig getAppConfig(long startTime, String configFile) throws MissingConfigException {
        try {
            AppConfig appConfig = ConfigHelper.loadConfig(configFile);
            long millis = System.currentTimeMillis() - startTime;
            String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            log.info("config loaded in {}", took);
            return appConfig;
        } catch (URISyntaxException | IOException | RuntimeException e) {
            log.error("load config failed {}", e.getMessage());
            System.exit(1);
            throw new MissingConfigException(e);
        }
    }

    private static String getConfigFile(String[] args) throws MissingConfigException {
        if (args.length > 0) {
            return args[0];
        }
        throw new MissingConfigException("no config file provided as first argument");
    }

    public static void configureLogging(LoggerConfig logger) {

        System.setProperty("tinylog.level", logger.getDefaultLevel().toString());
        logger.getLogs().forEach((pkg, level) -> {
            System.setProperty("tinylog.level@" + pkg, level);
        });
        logger.getWriter().forEach((pkg, level) -> {
            System.setProperty("tinylog.writerConsole." + pkg, level);
        });

        try {
            ReconfigurableLoggingProvider.reload();
        } catch (InterruptedException e) {
            log.info("on interrupt");
        } catch (ReflectiveOperationException e) {
            log.error("configure loging failed interrupt",e);
        }
    }
}

