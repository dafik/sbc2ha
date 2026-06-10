package iot.sbc2ha.hardware.io;

import iot.sbc2ha.runtime.DeviceState;

/**
 * Abstraction for a GPIO input pin.
 *
 * <p>Implementations bridge the hardware layer (e.g. diozero BBBioLib) to
 * the sbc2ha runtime. The fake runtime provides a no-op implementation
 * in {@code iot.sbc2ha.hardware.gpio} for tests that do not require real hardware.</p>
 *
 * <p>This interface is the boundary: core/domain/config packages must not
 * import diozero, and this interface contains no diozero types.</p>
 *
 * <h3>Event-driven I/O</h3>
 * <p>Real implementations (diozero-backed) use edge-triggered hardware
 * interrupts. Call {@link #addInputListener(InputListener)} to register
 * a listener that receives press/release events as the physical pin changes
 * state. Fake implementations must no-op for all listener methods.</p>
 */
public interface InputAdapter {

    /**
     * Reads the current logical state of this pin.
     *
     * @return the current state (ON = high/active, OFF = low/inactive)
     */
    DeviceState read();

    /**
     * Register a listener for edge-triggered press/release events.
     * <p>
     * Real implementations (e.g. diozero-backed) use hardware interrupts
     * to fire events when the pin state changes. Fake implementations
     * may store the listener but will not fire events — the test
     * harness uses {@link iot.sbc2ha.hardware.io.fake.FakeInputAdapter#setState}
     * to simulate state changes directly.
     *
     * @param listener the listener to register
     */
    default void addInputListener(InputListener listener) {
        // no-op for fake implementations
    }

    /**
     * Remove a previously registered listener.
     *
     * @param listener the listener to remove
     */
    default void removeInputListener(InputListener listener) {
        // no-op for fake implementations
    }

    /**
     * Releases resources held by this adapter (e.g. closes GPIO handle).
     */
    void close();

    /**
     * Listener for edge-triggered input state changes.
     *
     * <p>Events map to click detection semantics:
     * <ul>
     *   <li>{@code pressed(timestamp)} — pin went active (e.g. button pressed)</li>
     *   <li>{@code released(timestamp)} — pin went inactive (e.g. button released)</li>
     * </ul>
     */
    interface InputListener {
        /**
         * Called when the input pin becomes active (pressed).
         *
         * @param timestampMonotonic monotonic clock timestamp in milliseconds
         */
        void pressed(long timestampMonotonic);

        /**
         * Called when the input pin becomes inactive (released).
         *
         * @param timestampMonotonic monotonic clock timestamp in milliseconds
         */
        void released(long timestampMonotonic);
    }
}
