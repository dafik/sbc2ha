package com.dfi.sbc2ha.components.sensor.binary.click.detector;

import com.dfi.sbc2ha.components.sensor.Sensor;
import com.dfi.sbc2ha.components.sensor.binary.click.timer.ClickTimerTask;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

public class DoubleClickDetector implements ClickDetector {
    private final AtomicBoolean handleClickStarted = new AtomicBoolean(false);
    private final LongConsumer releaseConsumer;
    private final LongConsumer clickConsumer;
    private final LongConsumer doubleClickConsumer;
    private ClickTimerTask doubleClickTask;


    public DoubleClickDetector(int doubleClickDurationMs, int longPressDurationMs,
                               LongConsumer releaseConsumer,
                               LongConsumer clickConsumer, LongConsumer doubleClickConsumer, LongConsumer longPressConsumer) {
        this.releaseConsumer = releaseConsumer;

        this.clickConsumer = clickConsumer;
        this.doubleClickConsumer = doubleClickConsumer;
        createDoubleClickTimer(doubleClickDurationMs);
    }

    @Override
    public void detect(Long time, Sensor.Direction direction) {
        if (direction == Sensor.Direction.PRESS) {
            handlePress(time);
        }
    }


    private void whenClick(Long e) {
        clickConsumer.accept(e);
    }


    private void whenDoubleClick(Long e) {
        doubleClickConsumer.accept(e);
    }

    private void whenRelease(Long e) {
        releaseConsumer.accept(e);
    }


    private void handlePress(Long time) {
        if (!handleClickStarted.getAndSet(true)) {
            doubleClickTask.schedule();
        } else {
            if (doubleClickTask.isRunning()) {
                doubleClickTask.reset();
                handleClickStarted.set(false);
                whenDoubleClick(time);
                whenRelease(time);
            }
        }
    }

    private void createDoubleClickTimer(int doubleClickDurationMs) {
        doubleClickTask = new ClickTimerTask(time -> {
            doubleClickTask.reset();
            handleClickStarted.set(false);
            whenClick(time);
            whenRelease(time);
        }, doubleClickDurationMs);
    }


}
