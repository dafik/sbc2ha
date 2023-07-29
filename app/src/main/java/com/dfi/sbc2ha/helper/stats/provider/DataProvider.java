package com.dfi.sbc2ha.helper.stats.provider;

import com.dfi.sbc2ha.helper.Scheduler;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class DataProvider implements Runnable {
    protected final List<Consumer<List<?>>> listeners = new ArrayList<>();
    protected final HardwareAbstractionLayer hal;
    private final long initialDelay;
    private final long period;
    private final TimeUnit unit;
    private ScheduledFuture<?> scheduledFuture;

    public DataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        this.hal = hal;

        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;

    }

    public void addListener(Consumer<List<?>> consumer) {
        synchronized (listeners) {
            listeners.add(consumer);
        }
        schedule();
    }

    public void clearListeners() {
        stop();
        synchronized (listeners) {
            listeners.clear();
        }
    }

    public void schedule() {
        stop();
        scheduledFuture = Scheduler.getInstance().scheduleAtFixedRate(this, initialDelay, period, unit);

    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    protected void onChange() {
        List<?> lines = getLines();
        listeners.forEach(c -> c.accept(lines));
    }


    public abstract List<?> getLines();
}

