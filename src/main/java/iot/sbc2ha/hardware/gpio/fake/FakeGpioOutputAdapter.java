package iot.sbc2ha.hardware.gpio.fake;

import iot.sbc2ha.hardware.gpio.GpioOutputAdapter;
import iot.sbc2ha.runtime.DeviceState;

/**
 * Fake GPIO output adapter for hardware-independent testing.
 *
 * <p>Default state is {@link iot.sbc2ha.runtime.DeviceState#OFF}. Call
 * {@link #write(iot.sbc2ha.runtime.DeviceState)} to simulate a state change
 * written to hardware.</p>
 */
public final class FakeGpioOutputAdapter implements GpioOutputAdapter {

    private DeviceState state = DeviceState.OFF;

    @Override
    public void write(DeviceState state) {
        this.state = state;
    }

    @Override
    public DeviceState read() {
        return state;
    }

    /**
     * Returns the current state for test assertions.
     */
    public DeviceState getState() {
        return state;
    }

    @Override
    public void close() {
        // no-op
    }
}
