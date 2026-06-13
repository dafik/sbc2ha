package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;
import iot.sbc2ha.hardware.io.OutputDelegate;

/**
 * Creates pin-specific {@link OutputDelegate} instances from a pre-created
 * hardware chip or resolved pin info.
 *
 * <p>This factory does NOT know about I2C, addresses, or how the chip was
 * created — it only accepts an already-instantiated chip and builds a
 * pin-specific delegate.</p>
 *
 * <h3>Usage</h3>
 * <pre>
 * // MCP23017 (I2C expander):
 * MCP23017 chip = new MCP23017(i2cBus, i2cAddress, ...);
 * OutputDelegate delegate = OutputDelegateFactory.create(chip, globalPin);
 *
 * // Direct GPIO:
 * PinInfo pinInfo = board.getByPhysicalPin("P9", 12).get();
 * OutputDelegate delegate = OutputDelegateFactory.create(pinInfo, "P9_12");
 *
 * // Adapter (zero hardware knowledge):
 * OutputAdapter adapter = new DiozeroOutputAdapter(false, delegate);
 * </pre>
 *
 * @see OutputDelegate
 */
public final class OutputDelegateFactory {

    private OutputDelegateFactory() {
    }

    /**
     * Create a pin-specific {@link OutputDelegate} from a pre-created MCP23017 chip.
     *
     * @param mcp       the already-created MCP23017 chip instance (from registry or freshly created)
     * @param globalPin global pin number (0-15) on the chip
     * @return a pin-specific delegate
     */
    public static OutputDelegate create(MCP23017 mcp, int globalPin) {
        return new Mcp23017Bus(mcp, globalPin);
    }

    /**
     * Create a GPIO output delegate for a direct-board pin.
     *
     * @param pinInfo resolved pin info from the board definition
     * @param pinId   physical pin identifier (e.g. {@code "P9_12"})
     * @return a pin-specific delegate
     */
    public static OutputDelegate create(PinInfo pinInfo, String pinId) {
        return new GpioOutputBus(pinId, pinInfo);
    }
}
