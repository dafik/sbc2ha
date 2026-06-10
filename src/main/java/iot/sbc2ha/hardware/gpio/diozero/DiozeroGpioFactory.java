package iot.sbc2ha.hardware.gpio.diozero;

import iot.sbc2ha.hardware.gpio.GpioFactory;
import iot.sbc2ha.hardware.gpio.GpioInputAdapter;
import iot.sbc2ha.hardware.gpio.GpioOutputAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diozero-backed implementation of {@link GpioFactory}.
 *
 * <p>Creates real hardware adapters that directly drive GPIO pins.
 * The actual hardware platform is determined by the diozero provider
 * on the classpath (e.g. BBBioLib, RPi). Only input adapters are
 * implemented in this phase; output adapters are reserved for
 * MCP23017 output expansion.</p>
 *
 * <h3>Singleton</h3>
 * <p>Use {@link #INSTANCE} to obtain the singleton factory rather than
 * constructing new instances — this avoids repeated native GPIO
 * initialisation overhead.</p>
 */
@SuppressWarnings("unused")
public final class DiozeroGpioFactory implements GpioFactory {

    private static final Logger Log = LoggerFactory.getLogger(DiozeroGpioFactory.class);

    @SuppressWarnings("unused")
    public static final DiozeroGpioFactory INSTANCE = new DiozeroGpioFactory();

    private DiozeroGpioFactory() {
        Log.info("Diozero GPIO factory initialised");
    }

    @Override
    public GpioInputAdapter createInput(String pinId) {
        Log.debug("Creating Diozero GPIO input adapter for pin '{}'", pinId);
        return new DiozeroGpioInputAdapter(pinId, false);
    }

    @Override
    public GpioOutputAdapter createOutput(String pinId) {
        Log.debug("Creating Diozero GPIO output adapter for pin '{}' — not yet implemented", pinId);
        throw new UnsupportedOperationException(
                "Diozero GPIO output adapters are not yet implemented. "
                + "Use MCP23017 (SBC-017) for output expansion.");
    }
}
