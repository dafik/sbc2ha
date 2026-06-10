package iot.sbc2ha.hardware.io.diozero;

import iot.sbc2ha.hardware.io.InputOutputFactory;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.OutputAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diozero-backed implementation of {@link InputOutputFactory}.
 *
 * <p>Creates real hardware adapters that drive GPIO pins.
 * The actual hardware platform is determined by the diozero provider
 * on the classpath (e.g. BBBioLib, RPi).</p>
 *
 * <ul>
 *   <li><b>Input</b> — direct BBB GPIO via {@link DiozeroInputAdapter}</li>
 *   <li><b>Output</b> — MCP23017 I2C GPIO expander via {@link DiozeroOutputAdapter}</li>
 * </ul>
 *
 * <h3>Singleton</h3>
 * <p>Use {@link #INSTANCE} to obtain the singleton factory rather than
 * constructing new instances — this avoids repeated native GPIO
 * initialisation overhead.</p>
 */
@SuppressWarnings("unused")
public final class DiozeroInputOutputFactory implements InputOutputFactory {

    private static final Logger Log = LoggerFactory.getLogger(DiozeroInputOutputFactory.class);

    @SuppressWarnings("unused")
    public static final DiozeroInputOutputFactory INSTANCE = new DiozeroInputOutputFactory();

    private DiozeroInputOutputFactory() {
        Log.info("Diozero GPIO factory initialised");
    }

    @Override
    public InputAdapter createInput(String pinId) {
        Log.debug("Creating Diozero GPIO input adapter for pin '{}'", pinId);
        return new DiozeroInputAdapter(pinId, false);
    }

    @Override
    public OutputAdapter createOutput(String pinId) {
        Log.debug("Creating MCP23017 output adapter for location '{}'", pinId);
        return new DiozeroOutputAdapter(pinId, false);
    }
}
