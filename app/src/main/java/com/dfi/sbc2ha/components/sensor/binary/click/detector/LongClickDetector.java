package com.dfi.sbc2ha.components.sensor.binary.click.detector;

import com.dfi.sbc2ha.components.sensor.Sensor;
import com.dfi.sbc2ha.components.sensor.binary.click.timer.ClickTimerTask;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

public class LongClickDetector implements ClickDetector {
    private final AtomicBoolean handleClickStarted = new AtomicBoolean(false);
    private final LongConsumer releaseConsumer;
    private final LongConsumer clickConsumer;
    private final LongConsumer doubleClickConsumer;
    private final LongConsumer longPressConsumer;
    private ClickTimerTask doubleClickTask;
    private ClickTimerTask longPressTask;

    public LongClickDetector(int doubleClickDurationMs, int longPressDurationMs,
                             LongConsumer releaseConsumer,
                             LongConsumer clickConsumer, LongConsumer doubleClickConsumer, LongConsumer longPressConsumer) {
        this.releaseConsumer = releaseConsumer;

        this.clickConsumer = clickConsumer;
        this.doubleClickConsumer = doubleClickConsumer;
        this.longPressConsumer = longPressConsumer;

        createDoubleClickTimer(doubleClickDurationMs);
        createLongPressTimer(longPressDurationMs);
    }

    @Override
    public void detect(Long time, Sensor.Direction direction) {
        if (direction == Sensor.Direction.PRESS) {
            handlePress(time);
        } else {
            if (!doubleClickTask.isRunning()) {
                whenRelease(time);
                handleClickStarted.set(false);
            }
            longPressTask.reset();
        }
    }


    private void whenClick(Long e) {
        clickConsumer.accept(e);
    }

    private void whenDoubleClick(Long e) {
        doubleClickConsumer.accept(e);
    }


    private void whenPressLong(Long e) {
        longPressConsumer.accept(e);
    }

    private void whenRelease(Long e) {
        releaseConsumer.accept(e);
    }


    private void handlePress(Long time) {
        if (!handleClickStarted.getAndSet(true)) {
            doubleClickTask.schedule();
            longPressTask.schedule();
        } else {
            if (doubleClickTask.isRunning()) {
                doubleClickTask.reset();
                longPressTask.reset();

                handleClickStarted.set(false);
                whenDoubleClick(time);
            }
        }
    }

    private void createDoubleClickTimer(int doubleClickDurationMs) {
        doubleClickTask = new ClickTimerTask(time -> {
            doubleClickTask.reset();
            if (!longPressTask.isRunning()) {
                handleClickStarted.set(false);
                whenClick(time);
                whenRelease(time);
            }
        }, doubleClickDurationMs);
    }

    private void createLongPressTimer(int longPressDurationMs) {
        longPressTask = new ClickTimerTask(time -> {
            longPressTask.reset();
            handleClickStarted.set(false);
            whenPressLong(time);
        }, longPressDurationMs);
    }
}
