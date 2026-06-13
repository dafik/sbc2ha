package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.GpioEventTrigger;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.MCP23017;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.InputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pin-level delegate for MCP23017 I2C GPIO expander input.
 *
 * <p>Creates a pin-specific {@link DigitalInputDevice} backed by a shared
 * MCP23017 chip instance. Each instance is independent — the adapter that
 * creates it is responsible for closing it.</p>
 *
 * <p>Mirrors {@link Mcp23017Bus} but for input — handles edge-triggered
 * event delivery on the expander's input pins.</p>
 *
 * <h3>Pin addressing</h3>
 * <p>Global pin numbers 0-7 map to port A, 8-15 to port B.</p>
 *
 * @see InputDelegate
 */
public final class Mcp23017InputBus implements InputDelegate {

    private static final Logger log = LoggerFactory.getLogger(Mcp23017InputBus.class);

    private final int globalPin;
    private final DigitalInputDevice digitalInput;
    private final CopyOnWriteArrayList<InputAdapter.InputListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Create a pin-specific delegate for this MCP23017 chip.
     *
     * @param mcp       the shared MCP23017 chip instance
     * @param globalPin global pin number (0-15)
     */
    public Mcp23017InputBus(MCP23017 mcp, int globalPin) {
        this.globalPin = globalPin;
        Objects.requireNonNull(mcp, "mcp must not be null");

        PinInfo pinInfo = mcp.getBoardPinInfo().getByGpioNumber(globalPin)
                .orElse(null);
        if (pinInfo == null) {
            log.warn("MCP23017 has no pin for global GPIO {} — delegate will be non-functional",
                    globalPin);
            this.digitalInput = null;
        } else {
            this.digitalInput = DigitalInputDevice.Builder.builder(pinInfo)
                    .setDeviceFactory(mcp)
                    .setTrigger(GpioEventTrigger.BOTH)
                    .build();
            digitalInput.whenActivated(this::firePress);
            digitalInput.whenDeactivated(this::fireRelease);
        }
    }

    private void firePress(long timestamp) {
        log.debug("MCP23017 pin {} press event (ts={})", globalPin, timestamp);
        for (InputAdapter.InputListener listener : listeners) {
            try {
                listener.pressed(timestamp);
            } catch (Exception e) {
                log.warn("Error in InputListener.pressed for MCP23017 pin {}: {}", globalPin, e.getMessage());
            }
        }
    }

    private void fireRelease(long timestamp) {
        log.debug("MCP23017 pin {} release event (ts={})", globalPin, timestamp);
        for (InputAdapter.InputListener listener : listeners) {
            try {
                listener.released(timestamp);
            } catch (Exception e) {
                log.warn("Error in InputListener.released for MCP23017 pin {}: {}", globalPin, e.getMessage());
            }
        }
    }

    @Override
    public DeviceState read() {
        if (digitalInput == null) {
            return DeviceState.OFF;
        }
        try {
            return digitalInput.getValue() ? DeviceState.ON : DeviceState.OFF;
        } catch (RuntimeIOException e) {
            log.warn("Failed to read MCP23017 pin {}: {}", globalPin, e.getMessage());
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
            if (digitalInput != null) {
                digitalInput.close();
            }
            log.info("Closed MCP23017 input delegate (pin={})", globalPin);
        } catch (Exception e) {
            log.warn("Failed to close MCP23017 input delegate: {}", e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Mcp23017InputBus{pin=" + globalPin + ", functional=" + (digitalInput != null) + "}";
    }
}
