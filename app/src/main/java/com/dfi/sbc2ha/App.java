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
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class App {

    public static final String DIOZERO_DEVICEFACTORY = "diozero.devicefactory";
    private static final String DEFAULT_DEVICEFACTORY_CLASS = "com.diozero.internal.provider.builtin.DefaultDeviceFactory";


    public static void main(String[] args) {


        long startTime = System.currentTimeMillis();
        Logger.info("App starting {}", Version.VERSION);

        try {
            configureDeviceFactory();

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
        Logger.info("App started in {}", took);

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
                SleepUtil.sleepSeconds(1);
            } catch (RuntimeInterruptedException e) {
                Logger.error(e);
                System.exit(0);
            }

        }
    }

    private static AppConfig getAppConfig(long startTime, String configFile) throws MissingConfigException {
        try {
            AppConfig appConfig = ConfigHelper.loadConfig(configFile);
            long millis = System.currentTimeMillis() - startTime;
            String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            Logger.info("config loaded in {}", took);
            return appConfig;
        } catch (URISyntaxException | IOException | RuntimeException e) {
            Logger.error("load config failed {}", e.getMessage());
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


    private static void configureLogging(LoggerConfig config) {
        if (config == null) {
            return;
        }

        // Here, the original entries of `tinylog.properties` are overwritten via system properties. However, you can
        // also manipulate your `tinylog.properties` file directly, if it is in a writable location and not part of the
        // classpath.

        List<String> skip = new ArrayList<>();

        Map<String, String> originalConfig = ReconfigurableLoggingProvider.getOriginalConfig();

        System.setProperty("tinylog.level", config.getDefaultLevel().toString().toLowerCase());
        config.getLogs().forEach((pkg, level) -> {
            if (!pkg.contains("boneio")) {
                System.setProperty(pkg, level.toLowerCase());
            } else {
                skip.add(pkg + "=" + level);
            }
        });

        Map<String, String> reload;
        try {
            reload = ReconfigurableLoggingProvider.reload();
        } catch (InterruptedException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        if (!originalConfig.equals(reload)) {
            Logger.warn("Original: {}", originalConfig);
            Logger.warn("New: {}", reload);
            Logger.warn("Skipped: {}", skip);
        }
    }


}

