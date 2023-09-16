package com.dfi.sbc2ha.components.sensor.binary.click.detector;

import com.dfi.sbc2ha.components.sensor.Sensor;

import java.util.function.LongConsumer;

public class SingleClickDetector implements ClickDetector {

    private final LongConsumer releaseConsumer;
    private final LongConsumer clickConsumer;

    public SingleClickDetector(int doubleClickDurationMs, int longPressDurationMs, LongConsumer releaseConsumer,
                               LongConsumer clickConsumer, LongConsumer doubleClickConsumer, LongConsumer longPressConsumer) {
        this.releaseConsumer = releaseConsumer;

        this.clickConsumer = clickConsumer;
    }

    @Override
    public void detect(Long time, Sensor.Direction direction) {
        if (direction == Sensor.Direction.PRESS) {
            whenClick(time);
        } else {
            whenRelease(time);
        }
    }

    private void whenClick(Long e) {
        clickConsumer.accept(e);
    }

    private void whenRelease(Long e) {
        releaseConsumer.accept(e);
    }


}


