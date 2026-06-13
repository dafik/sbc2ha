package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;
import iot.sbc2ha.hardware.io.OutputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Pin-level delegate for MCP23017 I2C GPIO expander.
 *
 * <p>Looks up the shared MCP23017 chip from {@link HardwareComponentRegistry}
 * and creates a pin-specific {@link DigitalOutputDevice} backed by it.
 * Each instance is independent — the adapter that creates it is responsible
 * for closing it.</p>
 *
 * <p>This mirrors the old app's pin-specific device creation: the bus (MCP23017)
 * is shared, but each pin's device is a separate instance owned by the caller.</p>
 *
 * <h3>Pin addressing</h3>
 * <p>Global pin numbers 0-7 map to port A, 8-15 to port B.</p>
 *
 * @see HardwareComponentRegistry
 * @see OutputDelegate
 */
public final class Mcp23017Bus implements OutputDelegate {

    private static final Logger log = LoggerFactory.getLogger(Mcp23017Bus.class);

    private final int globalPin;  // 0-15 on the chip
    private final DigitalOutputDevice digitalOutput;
    private DeviceState state = DeviceState.OFF;

    /**
     * Create a pin-specific delegate for this MCP23017 chip.
     *
     * <p>The MCP23017 chip must be created and registered externally
     * (e.g. via {@link HardwareComponentRegistry}). This class only
     * creates the pin-specific {@link DigitalOutputDevice}.</p>
     *
     * @param mcp         the shared MCP23017 chip instance
     * @param globalPin   global pin number (0-15)
     */
    public Mcp23017Bus(MCP23017 mcp, int globalPin) {
        this.globalPin = globalPin;
        Objects.requireNonNull(mcp, "mcp must not be null");

        // Get PinInfo for the global GPIO pin
        PinInfo pinInfo = mcp.getBoardPinInfo().getByGpioNumber(globalPin)
                .orElse(null);
        if (pinInfo == null) {
            log.warn("MCP23017 has no pin for global GPIO {} — delegate will be non-functional",
                    globalPin);
            this.digitalOutput = null;
        } else {
            this.digitalOutput = DigitalOutputDevice.Builder.builder(pinInfo)
                    .setDeviceFactory(mcp)
                    .setActiveHigh(true)
                    .setInitialValue(false)
                    .build();
        }
    }

    // -----------------------------------------------------------------------
    // OutputDelegate implementation (pin-level)
    // -----------------------------------------------------------------------

    @Override
    public void write(DeviceState desiredState) {
        if (digitalOutput == null) {
            log.debug("Cannot write to non-functional MCP23017 delegate (pin={}) — tracking state in software only",
                    globalPin);
        } else {
            try {
                if (desiredState == DeviceState.ON) {
                    digitalOutput.on();
                } else {
                    digitalOutput.off();
                }
            } catch (Exception e) {
                log.error("Failed to write MCP23017 pin {}: {}", globalPin, e.getMessage());
            }
        }
        state = desiredState;
    }

    @Override
    public DeviceState read() {
        if (digitalOutput == null) {
            return state;
        }
        try {
            boolean raw = digitalOutput.isOn();
            state = raw ? DeviceState.ON : DeviceState.OFF;
        } catch (Exception e) {
            log.warn("Failed to read MCP23017 pin {}: {}", globalPin, e.getMessage());
        }
        return state;
    }

    @Override
    public void close() {
        try {
            if (digitalOutput != null) {
                digitalOutput.close();
            }
            log.info("Closed MCP23017 delegate (pin={})", globalPin);
        } catch (Exception e) {
            log.warn("Failed to close MCP23017 delegate: {}", e.getMessage());
        }
        // NOTE: Do NOT close the MCP23017 — it is shared via the registry.
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return "Mcp23017Bus{pin=" + globalPin + ", functional=" + (digitalOutput != null) + "}";
    }
}
