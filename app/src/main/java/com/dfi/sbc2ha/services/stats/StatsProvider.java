package com.dfi.sbc2ha.services.stats;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ScreenType;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.services.state.StateManager;
import com.dfi.sbc2ha.services.stats.provider.*;
import com.diozero.util.SleepUtil;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class StatsProvider implements Runnable {
    private final Map<ScreenType, DataProvider> providers = new HashMap<>();
    private final StateManager stateManager;
    private final List<String> ids;
    protected HardwareAbstractionLayer hal;
    private Future<?> process;

    public StatsProvider(StateManager stateManager, List<String> ids) {
        this.stateManager = stateManager;
        this.ids = ids;
    }

    public void start() {
        process = Scheduler.getInstance().submit(this);
    }

    public void stop() {
        if (process != null) {
            process.cancel(true);
        }
    }

    public void addListener(ScreenType screenType, Consumer<List<?>> consumer) {
        while (!providers.containsKey(screenType)) {
            SleepUtil.sleepSeconds(1);
        }
        DataProvider dataProvider = providers.get(screenType);
        dataProvider.addListener(consumer);
        dataProvider.schedule();
    }

    public void removeTypeListeners(ScreenType screenType) {
        DataProvider dataProvider = providers.get(screenType);
        dataProvider.stop();
        dataProvider.clearListeners();
    }

    public void clearListeners() {
        providers.forEach((t, p) -> p.clearListeners());
    }


    @Override
    public void run() {
        if (hal == null) {
            SystemInfo si = new SystemInfo();
            hal = si.getHardware();

            providers.put(ScreenType.UPTIME, new UptimeDataProvider(hal, 0, 2, TimeUnit.SECONDS));
            providers.put(ScreenType.CPU, new CpuDataProvider(hal, 1, 10, TimeUnit.SECONDS));
            providers.put(ScreenType.DISK, new DiskDataProvider(hal, 1, 10, TimeUnit.SECONDS));
            providers.put(ScreenType.MEMORY, new MemoryDataProvider(hal, 1, 10, TimeUnit.SECONDS));
            providers.put(ScreenType.NETWORK, new NetworkDataProvider(hal, 1, 10, TimeUnit.SECONDS));
            providers.put(ScreenType.SWAP, new SwapDataProvider(hal, 1, 10, TimeUnit.SECONDS));
            providers.put(ScreenType.OUTPUTS, new OutputDataProvider(stateManager, ids));

            UptimeDataProvider p = (UptimeDataProvider) providers.get(ScreenType.UPTIME);
            p.run();
        } else {
            providers.forEach((t, p) -> p.run());
        }
    }


    public DataProvider getProvider(ScreenType type) {
        return providers.get(type);
    }

    public void close() {
        stop();
        clearListeners();
        providers.forEach((screenType, dataProvider) -> {
            dataProvider.stop();
            dataProvider.clearListeners();
        });
        providers.clear();
    }
}

