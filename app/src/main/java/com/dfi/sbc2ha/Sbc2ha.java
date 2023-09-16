package com.dfi.sbc2ha;

import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.event.bus.ReloadCommand;
import com.dfi.sbc2ha.event.bus.RestartCommand;
import com.dfi.sbc2ha.event.bus.StopCommand;
import com.dfi.sbc2ha.exception.MissingConfigException;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.services.config.ConfigProvider;
import com.dfi.sbc2ha.services.manager.Manager;
import com.dfi.sbc2ha.util.JavaCmd;
import com.dfi.sbc2ha.util.LogUtil;
import com.dfi.sbc2ha.util.RandomEnumGenerator;
import com.dfi.sbc2ha.web.Server;
import com.diozero.api.RuntimeInterruptedException;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Sbc2ha implements AutoCloseable {

    public static final String DIOZERO_DEVICEFACTORY = "diozero.devicefactory";
    private static final String DEFAULT_DEVICEFACTORY_CLASS = "com.diozero.internal.provider.builtin.DefaultDeviceFactory";
    @Getter
    private static String configFile;
    @Getter
    private static Sbc2ha sbc2ha;
    private final Server server;
    private final Thread thread;
    private boolean doRestart = false;
    private Manager manager;


    private Sbc2ha(long startTime) {
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .installDefaultEventBus()
                .register(this);


        server = new Server(configFile);
        start(startTime);

        thread = Thread.currentThread();
    }

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("App starting {}", Version.VERSION);

            configFile = getConfigFile(args);
            sbc2ha = new Sbc2ha(startTime);

            long millis = System.currentTimeMillis() - startTime;
            String took = String.format("%d sec", TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            log.info("App started in {}", took);

            sbc2ha.runLoop();

        } catch (Exception e) {
            log.error("main exit", e);
            System.exit(0);
        }
    }

    private static void logRandom(Level level, int i) {
        try {
            final Class<?> clazz = Logger.class;
            final Method method = clazz.getMethod(level.toString().toLowerCase(), String.class);
            final Object result = method.invoke(log, "random test log " + i);
            // do something with result
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("log", e);
        }
    }

    private static AppConfig getAppConfig(long startTime) throws MissingConfigException {
        try {
            AppConfig appConfig = ConfigProvider.loadConfig(configFile);
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

    private void configureDeviceFactory() {
        String deviceFactoryClass = System.getProperty(DIOZERO_DEVICEFACTORY);
        if (deviceFactoryClass == null) {
            System.setProperty(DIOZERO_DEVICEFACTORY, DEFAULT_DEVICEFACTORY_CLASS);
        }
    }

    private void start(long startTime) {
        try {
            configureDeviceFactory();
            AppConfig appConfig = getAppConfig(startTime);
            LogUtil.configureLogging(appConfig.getLogger());

            manager = new Manager(appConfig);

        } catch (MissingConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void reload(ReloadCommand cmd) {
        try {
            manager.close();
            long startTime = System.currentTimeMillis();
            start(startTime);
        } catch (Exception e) {
            log.error("reload", e);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void restart(RestartCommand cmd) {
        doRestart = true;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void stop(StopCommand cmd) {
        close();
    }

    public void restart() {
        doRestart = false;
        List<String> command = JavaCmd.getCommand(new String[]{configFile});
        log.error(String.join(",", command));
        manager.close();
        server.close();
        try {
            new ProcessBuilder(command).inheritIO().start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    private void runLoop() {
        var isFake = ExtensionBoardInfo.getInstance().isFake();
        while (true) {
            try {
                if (doRestart) {
                    restart();
                }
                if (isFake) {
                    SleepUtil.sleepSeconds(3);
                    RandomEnumGenerator<Level> rndLevel = new RandomEnumGenerator<>(Level.class);
                    ThreadLocalRandom rndInt = ThreadLocalRandom.current();
                    logRandom(rndLevel.randomEnum(), rndInt.nextInt(1000, 10000));

                } else {
                    SleepUtil.sleepSeconds(10);
                }
            } catch (RuntimeInterruptedException e) {
                log.error("main loop interrupted");
                System.exit(0);
            }
        }
    }

    @Override
    public void close() {
        Scheduler.shutdownAll();
        Diozero.shutdown();
        thread.interrupt();
    }

    public Set<Manager.InitialState> getCurrentState() {
        return manager.getCurrentState();
    }
}

