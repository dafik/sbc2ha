package com.dfi.sbc2ha.helper.stats.provider;

import com.dfi.sbc2ha.helper.Scheduler;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class DataProvider<C> implements Runnable {
    protected final List<Consumer<C>> listeners = new ArrayList<>();
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

    public void addListener(Consumer<C> consumer) {
        synchronized (listeners) {
            listeners.add(consumer);
        }
        schedule();
    }

    public DataProvider<C> clearListeners() {
        stop();
        synchronized (listeners) {
            listeners.clear();
        }

        return this;
    }

    public DataProvider<C> schedule() {
        stop();
        scheduledFuture = Scheduler.getInstance().scheduleAtFixedRate(this, initialDelay, period, unit);
        return this;
    }

    public DataProvider<C> stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        return this;
    }

    protected void onChange() {
        C lines = getLines();
        listeners.forEach(c -> c.accept(lines));
    }


    public abstract C getLines();
}

