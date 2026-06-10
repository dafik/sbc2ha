package iot.sbc2ha.hardware.gpio;

/**
 * Factory for creating GPIO input and output adapters.
 *
 * <p>Produced implementations (e.g. diozero-based) create real hardware adapters.
 * Test implementations provide fake adapters for hardware-independent tests.</p>
 */
public interface GpioFactory {

    /**
     * Creates an input adapter for the given pin identifier.
     *
     * @param pinId the physical pin identifier (e.g. "P9_11" for BBB)
     * @return a new input adapter
     */
    GpioInputAdapter createInput(String pinId);

    /**
     * Creates an output adapter for the given pin identifier.
     *
     * @param pinId the physical pin identifier (e.g. "P9_11" for BBB)
     * @return a new output adapter
     */
    GpioOutputAdapter createOutput(String pinId);
}
