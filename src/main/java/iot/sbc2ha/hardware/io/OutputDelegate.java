package iot.sbc2ha.hardware.io;

import iot.sbc2ha.runtime.DeviceState;

/**
 * Abstraction for a hardware output delegate.
 *
 * <p>Represents a single output pin on a hardware device (MCP23017, PCA9685,
 * etc.). Adapters operate on this interface rather than concrete hardware
 * types — enabling any combination of hardware devices through the registry.</p>
 *
 * <h3>Old app parallel</h3>
 * <p>In the old app, {@code Bus<T>} registered hardware chips (e.g.
 * {@code MCP23017Bus} wrapping a diozero {@code MCP23017}). Actuators
 * looked up the bus, called {@code bus.getBus()} to get the chip, then
 * created pin-specific devices (e.g. {@code Relay}) backed by that chip.
 * This interface is the pin-level abstraction that replaces those
 * pin-specific device classes.</p>
 *
 * @see OutputAdapter
 */
public interface OutputDelegate {

    /**
     * Write the desired state to this output pin.
     *
     * @param state the state to write (ON = high/active, OFF = low/inactive)
     */
    void write(DeviceState state);

    /**
     * Read back the current state of this output pin.
     *
     * @return the current state, or the last known state if read fails
     */
    DeviceState read();

    /**
     * Release resources held by this delegate.
     */
    void close();
}
