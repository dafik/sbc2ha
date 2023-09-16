package com.dfi.sbc2ha.components.sensor;


import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.dfi.sbc2ha.helper.Scheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ScheduledSensor extends ScalarSensor implements Runnable {
    public static final Duration UPDATE_INTERVAL = DurationStyle.detectAndParse("60s");
    protected final Duration updateInterval;
    protected final AtomicBoolean stopScheduler;
    private ScheduledFuture<?> scheduledFuture;

    public ScheduledSensor(String name, Duration updateInterval) {
        super(name);
        this.updateInterval = updateInterval;
        stopScheduler = new AtomicBoolean(true);
    }

    @Override
    protected void setDelegateListener() {
        stopScheduler.set(false);
        scheduledFuture = Scheduler.getInstance()
                .scheduleAtFixedRate(this, updateInterval.toNanos(), updateInterval.toNanos(),
                        TimeUnit.NANOSECONDS);

    }

    protected void removeDelegateListener() {
        stopScheduler.set(true);
        scheduledFuture.cancel(true);
    }
}
