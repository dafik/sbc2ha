package iot.sbc2ha.hardware.io.fake;

import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.runtime.DeviceState;

/**
 * Fake GPIO input adapter for hardware-independent testing.
 *
 * <p>Default state is {@link DeviceState#OFF}. Call {@link #setState(DeviceState)}
 * to simulate hardware state changes.</p>
 */
public final class FakeInputAdapter implements InputAdapter {

    private DeviceState state = DeviceState.OFF;

    @Override
    public DeviceState read() {
        return state;
    }

    /**
     * Simulates a hardware state change.
     */
    public void setState(DeviceState state) {
        this.state = state;
    }

    @Override
    public void close() {
        // no-op
    }
}
