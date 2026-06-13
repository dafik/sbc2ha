package iot.sbc2ha.hardware.io;

import iot.sbc2ha.hardware.PhysicalChannel;

/**
 * Factory for creating GPIO input and output adapters.
 *
 * <p>Produced implementations (e.g. diozero-based) create real hardware adapters.
 * Test implementations provide fake adapters for hardware-independent tests.</p>
 */
public interface InputOutputFactory {

    /**
     * Creates an input adapter for the given pin identifier.
     *
     * @param pinId the physical pin identifier (e.g. "P9_11" for BBB)
     * @return a new input adapter
     */
    InputAdapter createInput(String pinId);

    /**
     * Creates an output adapter for the given physical channel.
     *
     * <p>The factory dispatches by {@link PhysicalChannel#channelType()} to create
     * the appropriate adapter (MCP23017, PCA9685, GPIO, etc.).</p>
     *
     * @param channel the physical channel definition (not null)
     * @return a new output adapter
     */
    OutputAdapter createOutput(PhysicalChannel channel);
}
