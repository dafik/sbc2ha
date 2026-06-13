package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.GpioEventTrigger;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diozero-backed GPIO input adapter.
 *
 * <p>Wraps a diozero {@link DigitalInputDevice} and translates its raw
 * boolean reading into {@link DeviceState} values.</p>
 *
 * <p>Supports inversion — when {@code inverted} is true, HIGH maps to
 * {@link DeviceState#OFF} and LOW to {@link DeviceState#ON}, modelling
 * active-low sensor wiring.</p>
 *
 * <p>This class is the boundary where diozero types enter the system.
 * All other packages must interact through {@link InputAdapter}.</p>
 *
 * <h3>Event-driven input</h3>
 * <p>On construction the native provider is initialised (if not
 * already), the pin is looked up by its physical name,
 * and a {@link DigitalInputDevice} is opened with edge-triggered
 * notifications on both rising and falling edges.</p>
 * <p>Diozero's {@code whenActivated} / {@code whenDeactivated} callbacks
 * fire {@link InputAdapter.InputListener} events whenever the pin changes
 * state. This is the primary event path — no polling needed.</p>
 *
 * <h3>Initialization</h3>
 * <p>On construction the native provider is initialised (if not
 * already), the pin is looked up by its physical name,
 * and a {@link DigitalInputDevice} is opened with edge-triggered
 * notifications on both rising and falling edges.</p>
 *
 * <h3>Pin-mode overrides</h3>
 * <p>BBB LCD cape pins (P8_37–P8_46) have empty mode lists in both the
 * kernel's gpiolib-cdev and diozero's board definitions.  This adapter
 * loads a {@code bbb-modes.txt} resource (inherited from the old app)
 * that independently defines valid modes per pin, injecting
 * {@code DIGITAL_INPUT} support so diozero's validation succeeds.</p>
 *
 * <h3>Chip number overrides (kernel 6.x)</h3>
 * <p>On kernel 6.x BBBs the gpiochip numbers differ from diozero's
 * embedded board definition (written for kernel 4.x + cape-universal).
 * This adapter auto-detects the kernel version and applies chip overrides
 * from {@code bbb-chip-mappings.txt}.  Overrides are enabled automatically
 * for kernel >= 6.0, or when the system property
 * {@code sbc2ha.bbb.chip-override} is set to {@code true}.</p>
 *
 * @see InputAdapter
 */
public final class DiozeroInputAdapter implements InputAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiozeroInputAdapter.class);

    /** Pattern for BBB header-pin notation: P8_37, P9_42, etc. */
    static final Pattern HEADER_PIN_PATTERN = Pattern.compile("^(P[89])_(\\d+)$");

    /** Pre-loaded pin-mode overrides from classpath resource. */
    private static final Map<String, java.util.Set<com.diozero.api.DeviceMode>> MODE_OVERRIDES;

    /** Pre-loaded chip + line offset overrides (header:pin → PinOverride). */
    private static final Map<String, PinModeOverrides.PinOverride> CHIP_OVERRIDES;

    static {
        MODE_OVERRIDES = PinModeOverrides.loadModes("");
        CHIP_OVERRIDES = PinModeOverrides.loadChipOverrides("");
    }

    private final DigitalInputDevice device;
    private final boolean inverted;
    private final String pinId;

    /** Thread-safe listener list for edge-triggered events */
    private final List<InputListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Create a GPIO input adapter backed by diozero.
     *
     * @param pinId    physical pin identifier (e.g. {@code "P9_11"} for BBB)
     * @param inverted {@code true} to invert the logical reading
     */
    public DiozeroInputAdapter(String pinId, boolean inverted) {
        this.pinId = pinId;
        this.inverted = inverted;
        // Trigger native provider initialisation
        DeviceFactoryHelper.getNativeDeviceFactory();
        BoardInfo board = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
        PinInfo pinInfo = resolvePin(board, pinId);
        if (pinInfo == null) {
            throw new IllegalArgumentException("Unknown pin: " + pinId);
        }
        // Apply pin-mode + chip overrides (e.g. BBB LCD cape pins with empty modes,
        // kernel 6.x chip number corrections)
        PinInfo resolved = PinModeOverrides.wrap(pinInfo, MODE_OVERRIDES, CHIP_OVERRIDES);
        log.info("Opening GPIO pin '{}' (sysfs={}, chip={}, inverted={})",
                pinId, resolved.getSysFsNumber(), resolved.getChip(), inverted);
        this.device = DigitalInputDevice.Builder.builder(resolved)
                .setTrigger(GpioEventTrigger.BOTH)
                .build();

        // Register edge-triggered callbacks via diozero API.
        // whenActivated (rising edge) → press event (e.g. button pressed)
        // whenDeactivated (falling edge) → release event (e.g. button released)
        // This assumes normally-open (active-high) wiring. For active-low,
        // the inversion logic is applied at the ClickDetector layer.
        device.whenActivated(this::firePress);
        device.whenDeactivated(this::fireRelease);
        log.debug("Registered edge-triggered callbacks for pin '{}' (BOTH edge)", pinId);
    }

    /**
     * Resolve a pin identifier to a {@link PinInfo}.
     *
     * <p>Tries two strategies in order:</p>
     * <ol>
     *   <li>{@code getByPhysicalPin("P8", 37)} for BBB header-pin notation (e.g. "P8_37")</li>
     *   <li>{@code getByName(kernelName)} for kernel device tree names (e.g. "LCD_DATA8")</li>
     * </ol>
     *
     * @param board the board info from the active diozero provider
     * @param pinId the pin identifier from the hardware profile
     * @return the resolved {@link PinInfo}, or {@code null} if no match
     */
    static PinInfo resolvePin(BoardInfo board, String pinId) {
        // Try BBB header-pin notation first: P8_37, P9_42
        Matcher m = HEADER_PIN_PATTERN.matcher(pinId);
        if (m.matches()) {
            String header = m.group(1);
            int physicalPin = Integer.parseInt(m.group(2));
            Optional<PinInfo> result = board.getByPhysicalPin(header, physicalPin);
            if (result.isPresent()) {
                return result.get();
            }
            log.debug("Header pin {}_{} not found in board definitions", header, physicalPin);
        }
        // Fall back to kernel device tree name
        PinInfo byName = board.getByName(pinId);
        if (byName != null) {
            log.debug("Resolved '{}' by kernel name", pinId);
            return byName;
        }
        return null;
    }

    private void firePress(long timestamp) {
        log.debug("GPIO '{}' press event (ts={})", pinId, timestamp);
        for (InputListener listener : listeners) {
            try {
                listener.pressed(timestamp);
            } catch (Exception e) {
                log.warn("Error in InputListener.pressed for '{}': {}", pinId, e.getMessage());
            }
        }
    }

    private void fireRelease(long timestamp) {
        log.debug("GPIO '{}' release event (ts={})", pinId, timestamp);
        for (InputListener listener : listeners) {
            try {
                listener.released(timestamp);
            } catch (Exception e) {
                log.warn("Error in InputListener.released for '{}': {}", pinId, e.getMessage());
            }
        }
    }

    @Override
    public DeviceState read() {
        try {
            boolean raw = device.getValue();
            return (inverted != raw) ? DeviceState.ON : DeviceState.OFF;
        } catch (RuntimeIOException e) {
            log.warn("Failed to read GPIO pin '{}': {}", pinId, e.getMessage());
            return DeviceState.OFF;
        }
    }

    @Override
    public void addInputListener(InputListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeInputListener(InputListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void close() {
        try {
            device.close();
        } catch (RuntimeIOException e) {
            log.warn("Failed to close GPIO pin '{}': {}", pinId, e.getMessage());
        }
        log.info("Closed GPIO input pin '{}'", pinId);
    }

    /**
     * Return the physical pin identifier this adapter wraps.
     */
    @SuppressWarnings("unused")
    public String pinId() {
        return pinId;
    }

    /**
     * Whether this adapter inverts readings.
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Number of registered listeners (for testing).
     */
    @SuppressWarnings("unused")
    int listenerCount() {
        return listeners.size();
    }
}
