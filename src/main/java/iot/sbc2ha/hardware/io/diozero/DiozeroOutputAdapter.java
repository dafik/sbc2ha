package iot.sbc2ha.hardware.io.diozero;

import iot.sbc2ha.hardware.io.OutputAdapter;
import iot.sbc2ha.hardware.io.OutputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Output adapter backed by an {@link OutputDelegate}.
 *
 * <p>Handles only inversion logic and state tracking — zero knowledge of
 * the underlying transport (I2C, GPIO, SPI, etc.). The delegate is created
 * externally (by a factory or manager) and injected here.</p>
 *
 * @see OutputAdapter
 * @see OutputDelegate
 */
public final class DiozeroOutputAdapter implements OutputAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiozeroOutputAdapter.class);

    private final boolean inverted;
    private final OutputDelegate delegate;

    // Cached logical state — updated on every write()
    private DeviceState state = DeviceState.OFF;

    // -----------------------------------------------------------------------
    // Package-private constructor (delegate created externally)
    // -----------------------------------------------------------------------

    /**
     * Create an output adapter with a pre-created delegate.
     *
     * @param inverted whether logical ON maps to physical LOW
     * @param delegate pre-created output delegate (not null)
     */
    DiozeroOutputAdapter(boolean inverted, OutputDelegate delegate) {
        this.inverted = inverted;
        this.delegate = delegate;
    }

    // -----------------------------------------------------------------------
    // OutputAdapter
    // -----------------------------------------------------------------------

    @Override
    public void write(DeviceState desiredState) {
        // Map logical state to physical (respecting inversion)
        DeviceState physicalState = inverted
                ? (desiredState == DeviceState.ON ? DeviceState.OFF : DeviceState.ON)
                : desiredState;

        try {
            delegate.write(physicalState);
        } catch (Exception e) {
            log.error("Failed to write via delegate ({}): {}", delegate, e.getMessage());
        }
        // Always update state — software tracks intent even when hardware is unavailable
        state = desiredState;
    }

    @Override
    public DeviceState read() {
        try {
            boolean raw = delegate.read() == DeviceState.ON;
            // Inversion: if inverted, logical ON means physical OFF (raw=false)
            state = (inverted != raw) ? DeviceState.ON : DeviceState.OFF;
        } catch (Exception e) {
            log.warn("Failed to read via delegate ({}): {}", delegate, e.getMessage());
            // state unchanged — stale state returned
        }
        return state;
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
     * Current logical state of this output pin.
     */
    public DeviceState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DiozeroOutputAdapter{delegate=" + delegate + "}";
    }
}
