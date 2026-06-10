package iot.sbc2ha.input;

import iot.sbc2ha.runtime.EventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SimpleClickDetector}.
 */
class SimpleClickDetectorTest {

    @Test
    void trigger_firesCorrectEventType() {
        List<EventType> fired = new ArrayList<>();
        SimpleClickDetector detector = new SimpleClickDetector(EventType.CLICK, fired::add);

        detector.trigger();

        assertEquals(1, fired.size());
        assertEquals(EventType.CLICK, fired.getFirst());
    }

    @Test
    void trigger_firesDouble() {
        List<EventType> fired = new ArrayList<>();
        SimpleClickDetector detector = new SimpleClickDetector(EventType.DOUBLE, fired::add);

        detector.trigger();

        assertEquals(1, fired.size());
        assertEquals(EventType.DOUBLE, fired.getFirst());
    }

    @Test
    void trigger_firesLong() {
        List<EventType> fired = new ArrayList<>();
        SimpleClickDetector detector = new SimpleClickDetector(EventType.LONG, fired::add);

        detector.trigger();

        assertEquals(1, fired.size());
        assertEquals(EventType.LONG, fired.getFirst());
    }

    @Test
    void trigger_firesRelease() {
        List<EventType> fired = new ArrayList<>();
        SimpleClickDetector detector = new SimpleClickDetector(EventType.RELEASE, fired::add);

        detector.trigger();

        assertEquals(1, fired.size());
        assertEquals(EventType.RELEASE, fired.getFirst());
    }

    @Test
    void trigger_withNullCallback_doesNothing() {
        // Should not throw
        assertDoesNotThrow(() -> new SimpleClickDetector(EventType.CLICK, null).trigger());
    }

    @Test
    void eventType_returnsConfiguredType() {
        SimpleClickDetector detector = new SimpleClickDetector(EventType.LONG, null);

        assertEquals(EventType.LONG, detector.eventType());
    }

    @Test
    void onPress_and_onRelease_areNoop() {
        List<EventType> fired = new ArrayList<>();
        SimpleClickDetector detector = new SimpleClickDetector(EventType.CLICK, fired::add);

        // These should not fire the callback in the fake impl
        detector.onPress(100L);
        detector.onRelease(200L);

        // Only trigger() fires
        detector.trigger();
        assertEquals(1, fired.size());
    }

    @Test
    void multipleTriggers_fireMultipleTimes() {
        List<EventType> fired = new ArrayList<>();
        SimpleClickDetector detector = new SimpleClickDetector(EventType.CLICK, fired::add);

        detector.trigger();
        detector.trigger();
        detector.trigger();

        assertEquals(3, fired.size());
        fired.forEach(e -> assertEquals(EventType.CLICK, e));
    }
}
