package iot.sbc2ha.runtime;

import iot.sbc2ha.device.OutputDevice;

/**
 * Runtime wrapper for a {@link OutputDevice}.
 *
 * <p>Maintains mutable {@link DeviceState} and provides
 * {@link #toggle()} and {@link #setState(DeviceState)} for state changes.</p>
 */
public final class OutputRuntime extends DeviceRuntime {

    private DeviceState state;

    /**
     * Creates an output runtime initialized to {@link DeviceState#OFF}.
     */
    public OutputRuntime(OutputDevice device) {
        this(device, DeviceState.OFF);
    }

    /**
     * Creates an output runtime with the given initial state.
     *
     * @param device the underlying output device
     * @param state  the initial state
     */
    public OutputRuntime(OutputDevice device, DeviceState state) {
        super(device);
        this.state = state;
    }

    /**
     * @return the current state of this output device
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
