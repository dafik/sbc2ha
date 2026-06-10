package iot.sbc2ha.input;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SimpleDebouncer} (synchronous, timestamp-based).
 *
 * <p>Adapted from diozero's {@code DebouncedDigitalInputDeviceTest}.</p>
 */
@SuppressWarnings("unused") // BiConsumer<Boolean, Long> callback where we only use the Boolean
class SimpleDebouncerTest {

    private final List<Boolean> events = new ArrayList<>();
    private SimpleDebouncer debouncer;

    private SimpleDebouncer create(int debounceMs) {
        events.clear();
        return new SimpleDebouncer(debounceMs, (val, _) -> events.add(val));
    }

    /**
     * Rapid toggles within debounce window — no events should fire.
     * Adapted from diozero's {@code testNoEvents}.
     */
    @Test
    void rapidTogglesWithinWindow_noEvents() {
        debouncer = create(200);

        // Toggle every 100ms (within 200ms debounce window)
        debouncer.accept(true, 0);
        debouncer.accept(false, 100_000_000);
        debouncer.accept(true, 200_000_000);
        debouncer.accept(false, 300_000_000);

        // No value was stable for 200ms — nothing fires
        assertTrue(events.isEmpty());
    }

    /**
     * Stable transitions — one event per settled value.
     * Adapted from diozero's {@code testOneEventPerSecond}.
     */
    @Test
    void stableTransitions_fireEachSettledValue() {
        debouncer = create(500);

        // Press at t=0, stable for 1s → should fire
        debouncer.accept(true, 0);
        // Release at t=1s, stable for 1s → should fire
        debouncer.accept(false, 1_000_000_000L);
        // Press again at t=2s, stable → should fire
        debouncer.accept(true, 2_000_000_000L);
        // Flush to fire the last pending value
        debouncer.flush(3_000_000_000L);

        assertEquals(3, events.size());
        assertEquals(List.of(true, false, true), events);
    }

    /**
     * Transitions just over debounce time — should fire.
     * Adapted from diozero's {@code testSimilarPeriod}.
     */
    @Test
    void transitionsJustOverDebounce_fire() {
        debouncer = create(200);

        // Press at t=0, held for 210ms (just over 200ms debounce)
        debouncer.accept(true, 0);
        // Release at t=210ms — previous press was stable for 210ms > 200ms
        debouncer.accept(false, 210_000_000);

        // Press should have fired (stable for 210ms)
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());
    }

    /**
     * Slow transitions — well over debounce window.
     * Adapted from diozero's {@code testSlowPeriod}.
     */
    @Test
    void slowTransitions_fireImmediately() {
        debouncer = create(50);

        // Press at t=0, held for 500ms
        debouncer.accept(true, 0);
        // Release at t=500ms
        debouncer.accept(false, 500_000_000);
        // Flush to fire the last pending value
        debouncer.flush(600_000_000);

        assertEquals(2, events.size());
        assertEquals(List.of(true, false), events);
    }

    /**
     * Duplicate events for the same value are ignored.
     * Adapted from diozero's {@code testIgnoreDuplicates}.
     */
    @Test
    void duplicateEvents_ignored() {
        debouncer = create(100);

        // Send multiple true events — only the first matters
        debouncer.accept(true, 0);
        debouncer.accept(true, 10_000_000);
        debouncer.accept(true, 20_000_000);

        // Wait for debounce to settle, then send false
        debouncer.accept(false, 200_000_000);
        debouncer.accept(false, 210_000_000);
        debouncer.accept(false, 220_000_000);

        // Only 1 event: true (fired when false arrived at t=200ms)
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());
    }

    /**
     * Bouncy switch — rapid blips that don't exceed debounce window.
     * Adapted from diozero's {@code bouncySwitch1}.
     */
    @Test
    void bouncySwitch_rapidBlipsSuppressed() {
        debouncer = create(30);

        // Simulate bouncy press: rapid true/false/true/false within 10ms
        debouncer.accept(true, 0);
        debouncer.accept(false, 1_000_000);
        debouncer.accept(true, 2_000_000);
        debouncer.accept(false, 3_000_000);
        debouncer.accept(true, 4_000_000);

        // None of these were stable for 30ms — no events
        assertTrue(events.isEmpty());

        // Now hold true for 50ms (> 30ms debounce)
        debouncer.accept(false, 55_000_000); // this triggers true to fire (stable since t=4ms for 51ms)
        assertEquals(1, events.size());
        assertEquals(true, events.getFirst());
    }

    /**
     * Edge case: first event at t=0 should not fire immediately.
     */
    @Test
    void firstEventDoesNotFireImmediately() {
        debouncer = create(25);

        debouncer.accept(true, 0);

        // First event starts debounce — doesn't fire until next event
        assertTrue(events.isEmpty());
    }

    /**
     * Edge case: value returns to original before debounce settles.
     */
    @Test
    void pressReleaseBeforeDebounce_noEvents() {
        debouncer = create(50);

        // Press and release within 10ms (well under 50ms debounce)
        debouncer.accept(true, 0);
        debouncer.accept(false, 10_000_000);

        // Neither value was stable for 50ms — no events
        assertTrue(events.isEmpty());
    }

    /**
     * Multiple cycles: press/release, then press/release again.
     */
    @Test
    void multipleCycles() {
        debouncer = create(25);

        // Cycle 1: press at 0, release at 100ms
        debouncer.accept(true, 0);
        debouncer.accept(false, 100_000_000);

        // Cycle 2: press at 200ms, release at 300ms
        debouncer.accept(true, 200_000_000);
        debouncer.accept(false, 300_000_000);

        // Flush to fire the last pending value
        debouncer.flush(400_000_000);

        assertEquals(4, events.size());
        assertEquals(List.of(true, false, true, false), events);
    }
}
