package iot.sbc2ha.hardware.io.fake;

import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.runtime.DeviceState;

/**
 * Fake GPIO input adapter for hardware-independent testing.
 *
 * <p>Default state is {@link DeviceState#OFF}. Call {@link #setState(DeviceState)}
 * to simulate hardware state changes.</p>
 *
 * <p>For tests that need to inject events into the {@link iot.sbc2ha.runtime.InputRuntime} or
 * {@link iot.sbc2ha.runtime.ActionEngine}, use {@link #simulatePress()} and
 * {@link #simulateRelease()} to fire listener callbacks.
 * For tests that just need to read a state, use {@link #setState}.</p>
 */
public final class FakeInputAdapter implements InputAdapter {

    private DeviceState state = DeviceState.OFF;

    /** Registered listeners — stored to support add/remove, but not fired in fake impl */
    private InputListener registeredListener;

    @Override
    public DeviceState read() {
        return state;
    }

    /**
     * Simulates a hardware state change.
     */
    @SuppressWarnings("unused")
    public void setState(DeviceState state) {
        this.state = state;
    }

    /**
     * Fire a press event to all registered listeners.
     * <p>
     * This simulates the hardware going active (rising edge).
     * Useful for tests that verify click detection or InputRuntime state tracking.
     *
     * @return this adapter for method chaining
     */
    @SuppressWarnings("unused")
    public FakeInputAdapter simulatePress() {
        if (registeredListener != null) {
            registeredListener.pressed(System.nanoTime());
        }
        return this;
    }

    /**
     * Fire a release event to all registered listeners.
     * <p>
     * This simulates the hardware going inactive (falling edge).
     * Useful for tests that verify click detection or InputRuntime state tracking.
     *
     * @return this adapter for method chaining
     */
    @SuppressWarnings("unused")
    public FakeInputAdapter simulateRelease() {
        if (registeredListener != null) {
            registeredListener.released(System.nanoTime());
        }
        return this;
    }

    @Override
    public void addInputListener(InputListener listener) {
        this.registeredListener = listener;
    }

    @Override
    public void removeInputListener(InputListener listener) {
        if (registeredListener == listener) {
            registeredListener = null;
        }
    }

    @Override
    public void close() {
        // no-op
    }

    /**
     * Returns the current state for test assertions.
     */
    public DeviceState getState() {
        return state;
    }
}
