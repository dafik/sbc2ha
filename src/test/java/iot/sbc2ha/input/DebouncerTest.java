package iot.sbc2ha.input;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Debouncer} (threaded version with timer).
 *
 * <p>Adapted from diozero's {@code DebouncedDigitalInputDeviceTest}.</p>
 */
class DebouncerTest {

    private final List<Boolean> events = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;
    private Debouncer debouncer;

    @BeforeEach
    void setUp() {
        events.clear();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach
    void tearDown() {
        if (debouncer != null) {
            debouncer.close();
        }
        scheduler.shutdownNow();
    }

    private Debouncer create(int debounceMs) {
        return new Debouncer(scheduler, debounceMs, events::add);
    }

    /**
     * Rapid toggles within debounce window — no events should fire.
     */
    @Test
    void rapidTogglesWithinWindow_noEvents() throws InterruptedException {
        debouncer = create(200);

        // Toggle every 100ms (within 200ms debounce window)
        debouncer.accept(true);
        Thread.sleep(100);
        debouncer.accept(false);
        Thread.sleep(100);
        debouncer.accept(true);
        Thread.sleep(100);
        debouncer.accept(false);

        // Wait for any pending timers
        Thread.sleep(250);

        // No value was stable for 200ms — nothing fires
        assertTrue(events.isEmpty());
    }

    /**
     * Stable transitions — one event per settled value.
     */
    @Test
    void stableTransitions_fireEachSettledValue() throws InterruptedException {
        debouncer = create(50);

        // Press — stable for 200ms (> 50ms debounce)
        debouncer.accept(true);
        Thread.sleep(200);
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());

        // Release — stable for 200ms (> 50ms debounce)
        debouncer.accept(false);
        Thread.sleep(200);
        assertEquals(2, events.size());
        assertEquals(false, events.get(1));
    }

    /**
     * Slow transitions — well over debounce window.
     */
    @Test
    void slowTransitions_fire() throws InterruptedException {
        debouncer = create(50);

        // Press at t=0, held for 500ms
        debouncer.accept(true);
        Thread.sleep(500);
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());

        // Release
        debouncer.accept(false);
        Thread.sleep(500);
        assertEquals(2, events.size());
        assertEquals(false, events.get(1));
    }

    /**
     * Duplicate events for the same value are ignored.
     */
    @Test
    void duplicateEvents_ignored() throws InterruptedException {
        debouncer = create(100);

        // Send multiple true events — only the first matters
        debouncer.accept(true);
        Thread.sleep(10);
        debouncer.accept(true);
        Thread.sleep(10);
        debouncer.accept(true);

        // Wait for debounce to settle
        Thread.sleep(200);
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());

        // Send multiple false events
        debouncer.accept(false);
        Thread.sleep(10);
        debouncer.accept(false);

        // Wait for debounce to settle
        Thread.sleep(200);
        assertEquals(2, events.size());
        assertEquals(false, events.get(1));
    }

    /**
     * Bouncy switch — rapid blips that don't exceed debounce window.
     */
    @Test
    void bouncySwitch_rapidBlipsSuppressed() throws InterruptedException {
        debouncer = create(30);

        // Simulate bouncy press: rapid true/false/true/false within 10ms
        debouncer.accept(true);
        Thread.sleep(1);
        debouncer.accept(false);
        Thread.sleep(1);
        debouncer.accept(true);
        Thread.sleep(1);
        debouncer.accept(false);
        Thread.sleep(1);
        debouncer.accept(true);

        // Wait for debounce to settle (30ms)
        Thread.sleep(50);
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());
    }

    /**
     * Multiple cycles: press/release, then press/release again.
     */
    @Test
    void multipleCycles() throws InterruptedException {
        debouncer = create(25);

        // Cycle 1: press, wait, release, wait
        debouncer.accept(true);
        Thread.sleep(100);
        debouncer.accept(false);
        Thread.sleep(100);

        // Cycle 2: press, wait, release, wait
        debouncer.accept(true);
        Thread.sleep(100);
        debouncer.accept(false);
        Thread.sleep(100);

        assertEquals(4, events.size());
        assertEquals(List.of(true, false, true, false), events);
    }
}
