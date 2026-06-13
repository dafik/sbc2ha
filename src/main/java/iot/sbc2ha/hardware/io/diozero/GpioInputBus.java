package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.GpioEventTrigger;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.sbc.BoardInfo;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.InputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pin-level delegate for a direct-board GPIO input pin.
 *
 * <p>Creates a {@link DigitalInputDevice} backed by the board's native GPIO
 * provider (no I2C expander). Each instance owns its device and is responsible
 * for closing it.</p>
 *
 * <p>Mirrors {@link GpioOutputBus} but for input — handles pin resolution,
 * mode overrides, and edge-triggered event delivery.</p>
 *
 * @see InputDelegate
 */
public final class GpioInputBus implements InputDelegate {

    private static final Logger log = LoggerFactory.getLogger(GpioInputBus.class);

    /** Pattern for BBB header-pin notation: P8_37, P9_42, etc. */
    static final Pattern HEADER_PIN_PATTERN = Pattern.compile("^(P[89])_(\\d+)$");

    private final DigitalInputDevice device;
    private final String pinId;
    private final CopyOnWriteArrayList<InputAdapter.InputListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Create a GPIO input delegate for the given pin.
     *
     * @param pinId   physical pin identifier (e.g. {@code "P9_11"} for BBB)
     * @param pinInfo resolved {@link PinInfo} from the board definition
     */
    public GpioInputBus(String pinId, PinInfo pinInfo) {
        this.pinId = pinId;
        if (pinInfo == null) {
            throw new IllegalArgumentException("pinInfo must not be null for pin: " + pinId);
        }
        log.info("Opening GPIO input pin '{}' (sysfs={}, chip={})",
                pinId, pinInfo.getSysFsNumber(), pinInfo.getChip());
        this.device = DigitalInputDevice.Builder.builder(pinInfo)
                .setTrigger(GpioEventTrigger.BOTH)
                .build();

        device.whenActivated(this::firePress);
        device.whenDeactivated(this::fireRelease);
        log.debug("Registered edge-triggered callbacks for pin '{}' (BOTH edge)", pinId);
    }

    private void firePress(long timestamp) {
        log.debug("GPIO '{}' press event (ts={})", pinId, timestamp);
        for (InputAdapter.InputListener listener : listeners) {
            try {
                listener.pressed(timestamp);
            } catch (Exception e) {
                log.warn("Error in InputListener.pressed for '{}': {}", pinId, e.getMessage());
            }
        }
    }

    private void fireRelease(long timestamp) {
        log.debug("GPIO '{}' release event (ts={})", pinId, timestamp);
        for (InputAdapter.InputListener listener : listeners) {
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
            return device.getValue() ? DeviceState.ON : DeviceState.OFF;
        } catch (RuntimeIOException e) {
            log.warn("Failed to read GPIO pin '{}': {}", pinId, e.getMessage());
            return DeviceState.OFF;
        }
    }

    @Override
    public void addInputListener(InputAdapter.InputListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeInputListener(InputAdapter.InputListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void close() {
        try {
            device.close();
            log.info("Closed GPIO input pin '{}'", pinId);
        } catch (RuntimeIOException e) {
            log.warn("Failed to close GPIO pin '{}': {}", pinId, e.getMessage());
        }
    }

    /**
     * Resolve a pin identifier to a {@link PinInfo}.
     *
     * @param board the board info from the active diozero provider
     * @param pinId the pin identifier from the hardware profile
     * @return the resolved {@link PinInfo}, or {@code null} if no match
     */
    public static PinInfo resolvePin(BoardInfo board, String pinId) {
        Matcher m = HEADER_PIN_PATTERN.matcher(pinId);
        if (m.matches()) {
            String header = m.group(1);
            int physicalPin = Integer.parseInt(m.group(2));
            var result = board.getByPhysicalPin(header, physicalPin);
            if (result.isPresent()) {
                return result.get();
            }
            log.debug("Header pin {}_{} not found in board definitions", header, physicalPin);
        }
        PinInfo byName = board.getByName(pinId);
        if (byName != null) {
            log.debug("Resolved '{}' by kernel name", pinId);
            return byName;
        }
        return null;
    }

    @Override
    public String toString() {
        return "GpioInputBus{pin='" + pinId + "'}";
    }
}
