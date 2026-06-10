package iot.sbc2ha.input;

import iot.sbc2ha.device.ClicksConfig;
import iot.sbc2ha.runtime.EventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SimpleClickDetector}.
 *
 * <p>Tests three detection modes:</p>
 * <ul>
 *   <li><b>CLICK_ONLY</b> — instant CLICK on press, no waiting</li>
 *   <li><b>CLICK+DOUBLE</b> — double-click window (350ms), then CLICK</li>
 *   <li><b>FULL</b> — double + long-press (700ms), LONG+RELEASE</li>
 * </ul>
 */
class SimpleClickDetectorTest {

    private final List<EventType> firedEvents = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;
    private SimpleClickDetector detector;

    @BeforeEach
    void setUp() {
        firedEvents.clear();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach
    void tearDown() {
        if (detector != null) {
            detector.shutdown();
        }
        scheduler.shutdownNow();
    }

    private SimpleClickDetector createDetector(ClicksConfig config) {
        return new SimpleClickDetector(scheduler, config, firedEvents::add);
    }

    // ============================================================
    // Mode: CLICK_ONLY (instant on press, no waiting)
    // ============================================================

    @Test
    void clickOnly_firesImmediatelyOnPress() {
        ClicksConfig config = new ClicksConfig(true, false, false, false);
        detector = createDetector(config);

        detector.onPress(0L);

        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());
    }

    @Test
    void clickOnly_releaseIsNoop() {
        ClicksConfig config = new ClicksConfig(true, false, false, false);
        detector = createDetector(config);

        detector.onPress(0L);
        detector.onRelease(100L);

        // Click already fired on press — release is ignored
        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());
    }

    // ============================================================
    // Mode: CLICK+DOUBLE (double-click window, no long-press)
    // ============================================================

    @Test
    void clickDouble_singlePressRelease_sendsClick() {
        ClicksConfig config = new ClicksConfig(true, true, false, false);
        detector = createDetector(config);

        detector.onPress(0L);
        detector.onRelease(100L); // release before double-click window

        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());
    }

    @Test
    void clickDouble_twoPressesWithinWindow_sendsDouble() {
        ClicksConfig config = new ClicksConfig(true, true, false, false);
        detector = createDetector(config);

        detector.onPress(0L);
        detector.onPress(200L); // within 350ms double-click window

        assertEquals(1, firedEvents.size());
        assertEquals(EventType.DOUBLE, firedEvents.getFirst());

        // Release after double — nothing more
        detector.onRelease(250L);
        assertEquals(1, firedEvents.size());
    }

    @Test
    void clickDouble_secondPressOutsideWindow_sendsTwoClicks() {
        ClicksConfig config = new ClicksConfig(true, true, false, false);
        detector = createDetector(config);

        // First click
        detector.onPress(0L);
        detector.onRelease(100L);
        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());

        // Second click
        detector.onPress(500L);
        detector.onRelease(600L);

        assertEquals(2, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.get(1));
    }

    @Test
    void clickDouble_timerExpiry_sendsClick() throws InterruptedException {
        ClicksConfig config = new ClicksConfig(true, true, false, false);
        detector = createDetector(config);

        detector.onPress(0L);
        // Wait longer than double-click window (350ms)
        Thread.sleep(400);

        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());
    }

    // ============================================================
    // Mode: FULL (click + double + long + release)
    // ============================================================

    @Test
    void full_shortPress_sendsClick() {
        ClicksConfig config = new ClicksConfig(true, true, true, false);
        detector = createDetector(config);

        detector.onPress(0L);
        detector.onRelease(100L); // release before any timeout

        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());
    }

    @Test
    void full_doubleClick_sendsDouble() {
        ClicksConfig config = new ClicksConfig(true, true, true, false);
        detector = createDetector(config);

        detector.onPress(0L);
        detector.onPress(200L); // within double-click window

        assertEquals(1, firedEvents.size());
        assertEquals(EventType.DOUBLE, firedEvents.getFirst());

        detector.onRelease(250L);
        assertEquals(1, firedEvents.size()); // nothing more on release
    }

    @Test
    void full_longPress_sendsLongThenRelease() throws InterruptedException {
        ClicksConfig config = new ClicksConfig(true, true, true, true);
        detector = createDetector(config);

        detector.onPress(0L);

        // Wait for long-press threshold (700ms)
        Thread.sleep(800);
        assertEquals(1, firedEvents.size());
        assertEquals(EventType.LONG, firedEvents.getFirst());

        // Release fires RELEASE
        detector.onRelease(1000L);
        assertEquals(2, firedEvents.size());
        assertEquals(EventType.RELEASE, firedEvents.get(1));
    }

    @Test
    void full_longPress_noReleaseEventIfDisabled() throws InterruptedException {
        ClicksConfig config = new ClicksConfig(true, true, true, false); // release=false
        detector = createDetector(config);

        detector.onPress(0L);
        Thread.sleep(800);
        assertEquals(1, firedEvents.size());
        assertEquals(EventType.LONG, firedEvents.getFirst());

        detector.onRelease(1000L);
        assertEquals(1, firedEvents.size()); // no RELEASE event
    }

    @Test
    void full_timerExpiry_sendsClick() throws InterruptedException {
        ClicksConfig config = new ClicksConfig(true, true, true, false);
        detector = createDetector(config);

        detector.onPress(0L);
        // Release before double-click window (but after long-press would fire if we waited)
        Thread.sleep(400);
        detector.onRelease(500L);

        // Click fires from double-click timer expiry (350ms)
        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());
    }

    // ============================================================
    // Mode: all disabled
    // ============================================================

    @Test
    void noDetection_firesNothing() {
        ClicksConfig config = new ClicksConfig(false, false, false, false);
        detector = createDetector(config);

        detector.onPress(0L);
        detector.onRelease(100L);

        assertTrue(firedEvents.isEmpty());
    }

    // ============================================================
    // Edge cases
    // ============================================================

    @Test
    void releaseWithoutPress_isNoop() {
        ClicksConfig config = new ClicksConfig(true, true, true, false);
        detector = createDetector(config);

        assertDoesNotThrow(() -> detector.onRelease(100L));
        assertTrue(firedEvents.isEmpty());
    }

    @Test
    void pressActive_afterPress() {
        ClicksConfig config = new ClicksConfig(true, true, true, false);
        detector = createDetector(config);

        detector.onPress(0L);
        assertTrue(detector.isPressActive());

        detector.onRelease(100L);
        assertFalse(detector.isPressActive());
    }

    @Test
    void clickOnly_pressActive_returnsFalse() {
        ClicksConfig config = new ClicksConfig(true, false, false, false);
        detector = createDetector(config);

        detector.onPress(0L);
        // CLICK_ONLY fires immediately and returns to IDLE
        assertFalse(detector.isPressActive());
    }

    @Test
    void multipleCycles_clickOnly() {
        ClicksConfig config = new ClicksConfig(true, false, false, false);
        detector = createDetector(config);

        detector.onPress(0L);   // CLICK
        detector.onPress(100L); // CLICK
        detector.onPress(200L); // CLICK

        assertEquals(3, firedEvents.size());
        firedEvents.forEach(e -> assertEquals(EventType.CLICK, e));
    }

    @Test
    void full_sequence_singleDoubleLong() throws InterruptedException {
        ClicksConfig config = new ClicksConfig(true, true, true, true);
        detector = createDetector(config);

        // Cycle 1: click
        detector.onPress(0L);
        detector.onRelease(50L);
        assertEquals(1, firedEvents.size());
        assertEquals(EventType.CLICK, firedEvents.getFirst());

        // Cycle 2: double
        detector.onPress(200L);
        detector.onPress(300L);
        detector.onRelease(350L);
        assertEquals(2, firedEvents.size());
        assertEquals(EventType.DOUBLE, firedEvents.get(1));

        // Cycle 3: long + release
        detector.onPress(500L);
        Thread.sleep(800);
        detector.onRelease(1400L);
        assertEquals(4, firedEvents.size());
        assertEquals(EventType.LONG, firedEvents.get(2));
        assertEquals(EventType.RELEASE, firedEvents.get(3));
    }
}
