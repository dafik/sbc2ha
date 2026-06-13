package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.PinInfo;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.InputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Input adapter backed by an {@link InputDelegate}.
 *
 * <p>Handles only inversion logic — zero knowledge of the underlying transport
 * (I2C, GPIO, SPI, etc.). The delegate is created externally (by a factory)
 * and injected here.</p>
 *
 * <p>This mirrors {@link DiozeroOutputAdapter}'s pattern: the adapter is
 * transport-agnostic and delegates all hardware interaction to the delegate.</p>
 *
 * <h3>Pin-mode overrides</h3>
 * <p>BBB LCD cape pins (P8_37–P8_46) have empty mode lists in both the
 * kernel's gpiolib-cdev and diozero's board definitions.  The GPIO delegate
 * loads a {@code bbb-modes.txt} resource (inherited from the old app)
 * that independently defines valid modes per pin, injecting
 * {@code DIGITAL_INPUT} support so diozero's validation succeeds.</p>
 *
 * <h3>Chip number overrides (kernel 6.x)</h3>
 * <p>On kernel 6.x BBBs the gpiochip numbers differ from diozero's
 * embedded board definition (written for kernel 4.x + cape-universal).
 * The GPIO delegate applies chip overrides from {@code bbb-chip-mappings.txt}.
 * Overrides are enabled automatically for kernel >= 6.0, or when the system
 * property {@code sbc2ha.bbb.chip-override} is set to {@code true}.</p>
 *
 * @see InputAdapter
 * @see InputDelegate
 */
public final class DiozeroInputAdapter implements InputAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiozeroInputAdapter.class);

    /** Pre-loaded pin-mode overrides from classpath resource. */
    private static final Map<String, java.util.Set<com.diozero.api.DeviceMode>> MODE_OVERRIDES;

    /** Pre-loaded chip + line offset overrides (header:pin → PinOverride). */
    private static final Map<String, PinModeOverrides.PinOverride> CHIP_OVERRIDES;

    static {
        MODE_OVERRIDES = PinModeOverrides.loadModes("");
        CHIP_OVERRIDES = PinModeOverrides.loadChipOverrides("");
    }

    private final boolean inverted;
    private final InputDelegate delegate;

    // -----------------------------------------------------------------------
    // Package-private constructor (delegate created externally)
    // -----------------------------------------------------------------------

    /**
     * Create an input adapter with a pre-created delegate.
     *
     * @param inverted whether logical ON maps to physical LOW
     * @param delegate pre-created input delegate (not null)
     */
    DiozeroInputAdapter(boolean inverted, InputDelegate delegate) {
        this.inverted = inverted;
        this.delegate = delegate;
    }

    // -----------------------------------------------------------------------
    // Public constructor for legacy GPIO-only usage (backward compatibility)
    // -----------------------------------------------------------------------

    /**
     * Create a GPIO input adapter backed by diozero.
     *
     * <p>Resolves the pin, applies mode/chip overrides, creates a
     * {@link GpioInputBus} delegate, and wraps it with inversion logic.</p>
     *
     * @param pinId    physical pin identifier (e.g. {@code "P9_11"} for BBB)
     * @param inverted {@code true} to invert the logical reading
     */
    public DiozeroInputAdapter(String pinId, boolean inverted) {
        this.inverted = inverted;
        // Trigger native provider initialisation
        DeviceFactoryHelper.getNativeDeviceFactory();
        BoardInfo board = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
        PinInfo pinInfo = GpioInputBus.resolvePin(board, pinId);
        if (pinInfo == null) {
            throw new IllegalArgumentException("Unknown pin: " + pinId);
        }
        // Apply pin-mode + chip overrides (e.g. BBB LCD cape pins with empty modes,
        // kernel 6.x chip number corrections)
        PinInfo resolved = PinModeOverrides.wrap(pinInfo, MODE_OVERRIDES, CHIP_OVERRIDES);
        log.info("Opening GPIO pin '{}' (sysfs={}, chip={}, inverted={})",
                pinId, resolved.getSysFsNumber(), resolved.getChip(), inverted);
        this.delegate = new GpioInputBus(pinId, resolved);
    }

    // -----------------------------------------------------------------------
    // InputAdapter
    // -----------------------------------------------------------------------

    @Override
    public DeviceState read() {
        try {
            boolean raw = delegate.read() == DeviceState.ON;
            // Inversion: if inverted, logical ON means physical OFF (raw=false)
            return (inverted != raw) ? DeviceState.ON : DeviceState.OFF;
        } catch (Exception e) {
            log.warn("Failed to read via delegate ({}): {}", delegate, e.getMessage());
            return DeviceState.OFF;
        }
    }

    @Override
    public void addInputListener(InputListener listener) {
        delegate.addInputListener(listener);
    }

    @Override
    public void removeInputListener(InputListener listener) {
        delegate.removeInputListener(listener);
    }

    @Override
    public void close() {
        try {
            delegate.close();
        } catch (Exception e) {
            log.warn("Failed to close delegate: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /**
     * Whether this adapter inverts readings.
     */
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public String toString() {
        return "DiozeroInputAdapter{delegate=" + delegate + "}";
    }
}
