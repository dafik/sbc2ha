package com.dfi.sbc2ha.components.sensor.binary.click.detector;

import com.dfi.sbc2ha.components.sensor.Sensor;
import com.dfi.sbc2ha.components.sensor.binary.click.timer.ClickTimerTask;

import java.util.function.LongConsumer;

public class ClickDetectorBBB implements ClickDetector {

    private final LongConsumer clickConsumer;
    private final LongConsumer doubleClickConsumer;
    private final LongConsumer longPressConsumer;
    private final ClickTimerTask timerDouble;
    private final ClickTimerTask timerLong;
    private boolean doubleClickRan = false;
    private boolean isWaitingForSecondClick = false;
    private Sensor.Direction state = Sensor.Direction.RELEASE;

    public ClickDetectorBBB(int doubleClickDurationMs, int longPressDurationMs,
                            LongConsumer clickConsumer, LongConsumer doubleClickConsumer, LongConsumer longPressConsumer) {

        this.clickConsumer = clickConsumer;
        this.doubleClickConsumer = doubleClickConsumer;
        this.longPressConsumer = longPressConsumer;


        timerDouble = new ClickTimerTask(e -> doubleClickPressCallback(), doubleClickDurationMs);
        timerLong = new ClickTimerTask(this::whenPressLong, longPressDurationMs);

    }

    private void doubleClickPressCallback() {
        isWaitingForSecondClick = false;
        if (state == Sensor.Direction.RELEASE && !timerLong.isRunning()) {
            whenClick(1L);
        }
    }

    @Override
    public void detect(Long time, Sensor.Direction direction) {
        if (state == direction) {
            return;
        }
        state = direction;
        if (state == Sensor.Direction.PRESS) {
            timerLong.schedule();
            if (timerDouble.isRunning()) {
                timerDouble.reset();
                doubleClickRan = true;
                whenDoubleClick(time);
            } else {
                timerDouble.schedule();
                isWaitingForSecondClick = true;
            }
        } else {
            if (!isWaitingForSecondClick && !doubleClickRan) {
                if (timerLong.isRunning()) {
                    whenClick(time);
                }
            }
            timerLong.reset();
            doubleClickRan = false;
        }
    }


    public void whenClick(Long e) {
        clickConsumer.accept(e);
    }


    public void whenDoubleClick(Long e) {
        doubleClickConsumer.accept(e);
    }


    public void whenPressLong(Long e) {
        longPressConsumer.accept(e);
    }
}
