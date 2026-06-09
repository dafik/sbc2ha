package iot.sbc2ha.runtime;

import iot.sbc2ha.device.LightDevice;

/**
 * Runtime wrapper for a {@link LightDevice}.
 *
 * <p>Maintains mutable {@link DeviceState} and provides
 * {@link #toggle()} and {@link #setState(DeviceState)} for state changes.
 * Behaves identically to {@link OutputRuntime} but represents
 * a logical light entity rather than a physical pin.</p>
 */
public final class LightRuntime extends DeviceRuntime {

    private DeviceState state;

    /**
     * Creates a light runtime initialized to {@link DeviceState#OFF}.
     */
    public LightRuntime(LightDevice device) {
        this(device, DeviceState.OFF);
    }

    /**
     * Creates a light runtime with the given initial state.
     *
     * @param device the underlying light device
     * @param state  the initial state
     */
    public LightRuntime(LightDevice device, DeviceState state) {
        super(device);
        this.state = state;
    }

    /**
     * @return the current state of this light
     */
    public DeviceState state() {
        return state;
    }

    /**
     * Set the state directly.
     *
     * @param state the new state
     */
    public void setState(DeviceState state) {
        this.state = state;
    }

    /**
     * Toggle the state: ON → OFF, OFF → ON.
     *
     * @return the new state after toggle
     */
    public DeviceState toggle() {
        this.state = (this.state == DeviceState.ON) ? DeviceState.OFF : DeviceState.ON;
        return this.state;
    }
}
