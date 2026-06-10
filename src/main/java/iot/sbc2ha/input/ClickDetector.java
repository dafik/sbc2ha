package iot.sbc2ha.input;

import iot.sbc2ha.runtime.EventType;

/**
 * Detects switch/button events from raw hardware state changes.
 *
 * <p>The detection algorithm is pluggable. Implementations receive raw
 * hardware transitions (press/release) and fire {@link EventType} events
 * via the registered callback when a recognized pattern is detected.</p>
 *
 * <pre>
 * hardware.press() → detector.onPress(time)
 * hardware.release() → detector.onRelease(time)
 * → callback.accept(EventType.CLICK)
 * </pre>
 */
public interface ClickDetector {

    /**
     * Called when the hardware detects a press (active) transition.
     *
     * @param timestampMonotonic monotonic clock timestamp in milliseconds
     */
    void onPress(long timestampMonotonic);

    /**
     * Called when the hardware detects a release (inactive) transition.
     *
     * @param timestampMonotonic monotonic clock timestamp in milliseconds
     */
    void onRelease(long timestampMonotonic);
}
