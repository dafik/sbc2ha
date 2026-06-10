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
import java.util.concurrent.CopyOnWriteArrayList;

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
 * @see InputAdapter
 */
public final class DiozeroInputAdapter implements InputAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiozeroInputAdapter.class);

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
        PinInfo pinInfo = board.getByName(pinId);
        if (pinInfo == null) {
            throw new IllegalArgumentException("Unknown pin: " + pinId);
        }
        log.info("Opening GPIO pin '{}' (sysfs={}, inverted={})",
                pinId, pinInfo.getSysFsNumber(), inverted);
        this.device = DigitalInputDevice.Builder.builder(pinInfo)
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
