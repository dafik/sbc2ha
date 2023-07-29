package com.dfi.sbc2ha.helper.detector;

import com.dfi.sbc2ha.state.sensor.ButtonState;

import java.util.function.LongConsumer;

public class ClickDetectorFactory {
    public static ClickDetector build(ButtonState type, int doubleClickDurationMs, int longPressDurationMs,
                                      LongConsumer releaseConsumer,
                                      LongConsumer clickConsumer, LongConsumer doubleClickConsumer, LongConsumer longPressConsumer) {
        switch (type) {
            case SINGLE:
                return new SingleClickDetector(doubleClickDurationMs, longPressDurationMs,
                        releaseConsumer,
                        clickConsumer, doubleClickConsumer, longPressConsumer);
            case DOUBLE:
                return new DoubleClickDetector(doubleClickDurationMs, longPressDurationMs,
                        releaseConsumer,
                        clickConsumer, doubleClickConsumer, longPressConsumer);
            case LONG:
            default:
                return new LongClickDetector(doubleClickDurationMs, longPressDurationMs,
                        releaseConsumer,
                        clickConsumer, doubleClickConsumer, longPressConsumer);

        }
    }
}
