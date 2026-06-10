package iot.sbc2ha.hardware.gpio;

import iot.sbc2ha.runtime.DeviceState;

/**
 * Abstraction for a GPIO output pin.
 *
 * <p>Implementations bridge the hardware layer (e.g. diozero BBBioLib) to
 * the sbc2ha runtime. The fake runtime provides a no-op implementation
 * in {@code iot.sbc2ha.hardware.gpio} for tests that do not require real hardware.</p>
 *
 * <p>This interface is the boundary: core/domain/config packages must not
 * import diozero, and this interface contains no diozero types.</p>
 */
public interface GpioOutputAdapter {

    /**
     * Sets the output state of this pin.
     *
     * @param state the desired state (ON = high/active, OFF = low/inactive)
     */
    void write(DeviceState state);

    /**
     * Reads back the current output state.
     *
     * @return the current output state
     */
    DeviceState read();

    /**
     * Releases resources held by this adapter (e.g. closes GPIO handle).
     */
    void close();
}
