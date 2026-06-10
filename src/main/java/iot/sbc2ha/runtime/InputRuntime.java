package iot.sbc2ha.runtime;

import iot.sbc2ha.device.InputDevice;

/**
 * Runtime wrapper for an {@link InputDevice}.
 *
 * <p>Tracks the raw hardware state and exposes the logical state
 * after applying the device's inversion configuration.</p>
 */
public final class InputRuntime extends DeviceRuntime {

    private final InputDevice device;
    private DeviceState rawState;

    /**
     * Creates an input runtime initialized to {@link DeviceState#OFF}.
     */
    public InputRuntime(InputDevice device) {
        this(device, DeviceState.OFF);
    }

    /**
     * Creates an input runtime with the given initial raw state.
     *
     * @param device  the underlying input device
     * @param rawState the raw hardware state
     */
    public InputRuntime(InputDevice device, DeviceState rawState) {
        super(device);
        this.device = device;
        this.rawState = rawState;
    }

    /**
     * @return the current logical (inversion-applied) state
     */
    public DeviceState state() {
        return device.logicalState(rawState);
    }

    /**
     * @return the raw hardware state (before inversion)
     */
    public DeviceState rawState() {
        return rawState;
    }

    /**
     * Update the raw hardware state.
     * <p>
     * This is how the hardware layer notifies the runtime of a sensor change.
     * The logical state {@link #state()} applies inversion automatically.
     *
     * @param rawState the new raw hardware state
     */
    public void setRawState(DeviceState rawState) {
        this.rawState = rawState;
    }

    @Override
    public InputDevice config() {
        return (InputDevice) super.config();
    }
}
