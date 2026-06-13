package iot.sbc2ha.hardware.io;

import iot.sbc2ha.runtime.DeviceState;

/**
 * Abstraction for a hardware input delegate.
 *
 * <p>Represents a single input pin on a hardware device (MCP23017, GPIO,
 * etc.). Adapters operate on this interface rather than concrete hardware
 * types — enabling any combination of hardware devices through the registry.</p>
 *
 * <p>This is the input-side analogue of {@link OutputDelegate}: the adapter
 * handles inversion and state tracking, while the delegate handles transport
 * (I2C, GPIO, SPI) and edge-triggered event delivery.</p>
 *
 * @see InputAdapter
 */
public interface InputDelegate {

    /**
     * Read the current raw state of this input pin.
     *
     * @return the current raw state (before inversion)
     */
    DeviceState read();

    /**
     * Register a listener for edge-triggered press/release events.
     *
     * @param listener the listener to register
     */
    void addInputListener(InputAdapter.InputListener listener);

    /**
     * Remove a previously registered listener.
     *
     * @param listener the listener to remove
     */
    void removeInputListener(InputAdapter.InputListener listener);

    /**
     * Release resources held by this delegate.
     */
    void close();
}
