package com.dfi.sbc2ha.helper.stats;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ScreenType;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.helper.StateManager;
import com.dfi.sbc2ha.helper.stats.provider.*;
import com.diozero.util.SleepUtil;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class StatsProvider implements Runnable {
    private final Map<ScreenType, DataProvider<?>> providers = new HashMap<>();
    private final StateManager stateManager;
    private final List<String> ids;
    protected HardwareAbstractionLayer hal;

    public StatsProvider(StateManager stateManager, List<String> ids) {
        this.stateManager = stateManager;
        this.ids = ids;
        Scheduler.getInstance().submit(this);
    }

    public <T> void addListener(ScreenType screenType, Consumer<T> consumer) {
        while (!providers.containsKey(screenType)) {
            SleepUtil.sleepSeconds(1);
        }
        DataProvider<T> dataProvider = (DataProvider<T>) providers.get(screenType);
        dataProvider.addListener(consumer);
        dataProvider.schedule();
    }

    public void removeTypeListeners(ScreenType screenType) {
        providers.get(screenType)
                .stop()
                .clearListeners();
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

}

