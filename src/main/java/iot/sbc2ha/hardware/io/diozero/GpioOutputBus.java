package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.PinInfo;
import iot.sbc2ha.hardware.io.OutputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pin-level delegate for a direct-board GPIO output pin.
 *
 * <p>Creates a {@link DigitalOutputDevice} backed by the board's native GPIO
 * provider (no I2C expander). Each instance owns its device and is responsible
 * for closing it.</p>
 *
 * <p>Mirrors {@link Mcp23017Bus} but operates on board-native pins instead of
 * an I2C expander chip.</p>
 *
 * @see OutputDelegate
 */
public final class GpioOutputBus implements OutputDelegate {

    private static final Logger log = LoggerFactory.getLogger(GpioOutputBus.class);

    private final DigitalOutputDevice digitalOutput;
    private final String pinId;
    private DeviceState state = DeviceState.OFF;

    /**
     * Create a GPIO output delegate for the given pin.
     *
     * @param pinId   physical pin identifier (e.g. {@code "P9_12"} for BBB)
     * @param pinInfo resolved {@link PinInfo} from the board definition
     */
    public GpioOutputBus(String pinId, PinInfo pinInfo) {
        this.pinId = pinId;
        if (pinInfo == null) {
            throw new IllegalArgumentException("pinInfo must not be null for pin: " + pinId);
        }
        log.info("Creating GPIO output for pin '{}' (sysfs={}, chip={})",
                pinId, pinInfo.getSysFsNumber(), pinInfo.getChip());
        this.digitalOutput = DigitalOutputDevice.Builder.builder(pinInfo)
                .setActiveHigh(true)
                .setInitialValue(false)
                .build();
    }

    @Override
    public void write(DeviceState desiredState) {
        try {
            if (desiredState == DeviceState.ON) {
                digitalOutput.on();
            } else {
                digitalOutput.off();
            }
        } catch (Exception e) {
            log.error("Failed to write GPIO pin '{}': {}", pinId, e.getMessage());
        }
        state = desiredState;
    }

    @Override
    public DeviceState read() {
        try {
            boolean raw = digitalOutput.isOn();
            state = raw ? DeviceState.ON : DeviceState.OFF;
        } catch (Exception e) {
            log.warn("Failed to read GPIO pin '{}': {}", pinId, e.getMessage());
        }
        return state;
    }

    @Override
    public void close() {
        try {
            digitalOutput.close();
            log.info("Closed GPIO output pin '{}'", pinId);
        } catch (Exception e) {
            log.warn("Failed to close GPIO output pin '{}': {}", pinId, e.getMessage());
        }
    }

    /**
     * Current tracked state.
     */
    DeviceState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "GpioOutputBus{pin='" + pinId + "'}";
    }
}
