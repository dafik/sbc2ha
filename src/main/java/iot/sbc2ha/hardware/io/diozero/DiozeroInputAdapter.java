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
 * <h3>Initialization</h3>
 * <p>On construction the native provider is initialised (if not
 * already), the pin is looked up by its physical name,
 * and a {@link DigitalInputDevice} is opened with edge-triggered
 * notifications on both rising and falling edges.</p>
 *
 * @see InputAdapter
 */
public final class DiozeroInputAdapter implements InputAdapter {

    private static final Logger Log = LoggerFactory.getLogger(DiozeroInputAdapter.class);

    private final DigitalInputDevice device;
    private final boolean inverted;
    private final String pinId;

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
        Log.info("Opening GPIO pin '{}' (sysfs={}, inverted={})",
                pinId, pinInfo.getSysFsNumber(), inverted);
        this.device = DigitalInputDevice.Builder.builder(pinInfo)
                .setTrigger(GpioEventTrigger.BOTH)
                .build();
    }

    @Override
    public DeviceState read() {
        try {
            boolean raw = device.getValue();
            return (inverted != raw) ? DeviceState.ON : DeviceState.OFF;
        } catch (RuntimeIOException e) {
            Log.warn("Failed to read GPIO pin '{}': {}", pinId, e.getMessage());
            return DeviceState.OFF;
        }
    }

    @Override
    public void close() {
        try {
            device.close();
        } catch (RuntimeIOException e) {
            Log.warn("Failed to close GPIO pin '{}': {}", pinId, e.getMessage());
        }
        Log.info("Closed GPIO input pin '{}'", pinId);
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
}
