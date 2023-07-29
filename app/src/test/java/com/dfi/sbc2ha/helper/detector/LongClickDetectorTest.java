package com.dfi.sbc2ha.helper.detector;

import com.dfi.sbc2ha.sensor.Sensor;
import com.diozero.util.SleepUtil;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.function.LongConsumer;

import static com.dfi.sbc2ha.sensor.binary.Button.DOUBLE_CLICK_DURATION_MS;
import static com.dfi.sbc2ha.sensor.binary.Button.LONG_PRESS_DURATION_MS;

@Slf4j
public class LongClickDetectorTest extends TestCase {
    LongConsumer release = (e) -> log.info("release");
    LongConsumer click = (e) -> log.info("click");
    LongConsumer doubleClick = (e) -> log.info("doubleClick");
    LongConsumer longPress = (e) -> log.info("longPress");


    @Test
    public void testClick() {
        var detector = new LongClickDetector(DOUBLE_CLICK_DURATION_MS, LONG_PRESS_DURATION_MS,
                release, click, doubleClick, longPress);

        detector.detect(0L, Sensor.Direction.PRESS);
        detector.detect(0L, Sensor.Direction.RELEASE);

        SleepUtil.sleepSeconds(1);

        detector.detect(0L, Sensor.Direction.PRESS);
        detector.detect(0L, Sensor.Direction.RELEASE);
        SleepUtil.sleepMillis(DOUBLE_CLICK_DURATION_MS+1);
        detector.detect(0L, Sensor.Direction.PRESS);
        detector.detect(0L, Sensor.Direction.RELEASE);

    }
    @Test
    public void testDoubleClick() {
        var detector = new LongClickDetector(DOUBLE_CLICK_DURATION_MS, LONG_PRESS_DURATION_MS,
                release, click, doubleClick, longPress);

        detector.detect(0L, Sensor.Direction.PRESS);
        detector.detect(0L, Sensor.Direction.RELEASE);
        detector.detect(0L, Sensor.Direction.PRESS);
        detector.detect(0L, Sensor.Direction.RELEASE);

        SleepUtil.sleepSeconds(1);

        detector.detect(0L, Sensor.Direction.PRESS);
        detector.detect(0L, Sensor.Direction.RELEASE);

        SleepUtil.sleepMillis(DOUBLE_CLICK_DURATION_MS);
    }

    @Test
    public void testLong1() {
        var detector = new LongClickDetector(DOUBLE_CLICK_DURATION_MS, LONG_PRESS_DURATION_MS,
                release, click, doubleClick, longPress);

        detector.detect(0L, Sensor.Direction.PRESS);
        SleepUtil.sleepMillis(LONG_PRESS_DURATION_MS+1);
        detector.detect(0L, Sensor.Direction.RELEASE);


        SleepUtil.sleepMillis(LONG_PRESS_DURATION_MS+1);

    }

}
