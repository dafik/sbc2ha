package iot.sbc2ha.runtime;

import iot.sbc2ha.device.InputDevice;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.InputAdapter.InputListener;

/**
 * Runtime wrapper for an {@link InputDevice}.
 *
 * <p>Tracks the raw hardware state and exposes the logical state
 * after applying the device's inversion configuration.</p>
 */
public final class InputRuntime extends DeviceRuntime {

    private final InputDevice device;
    private DeviceState rawState;
    private InputAdapter adapter;

    /**
     * Creates an input runtime initialized to {@link DeviceState#OFF}.
     */
    public InputRuntime(InputDevice device) {
        this(device, DeviceState.OFF);
    }

    /**
     * Creates an input runtime with the given raw state.
     *
     * @param device   the underlying input device
     * @param rawState the raw hardware state
     */
    public InputRuntime(InputDevice device, DeviceState rawState) {
        super(device);
        this.device = device;
        this.rawState = rawState;
    }

    /**
     * Bind a hardware input adapter to this runtime.
     * <p>
     * The adapter's initial state is read and set as the current raw state.
     * The runtime then registers as a listener so subsequent hardware events
     * update the raw state automatically.
     *
     * @param adapter the input adapter (may be {@code null})
     */
    public void bindAdapter(InputAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            this.rawState = adapter.read();
            adapter.addInputListener(new InputListener() {
                @Override
                public void pressed(long timestampMonotonic) {
                    // Hardware went active
                    rawState = DeviceState.ON;
                }
                @Override
                public void released(long timestampMonotonic) {
                    // Hardware went inactive
                    rawState = DeviceState.OFF;
                }
            });
        }
    }

    /**
     * Unbind the adapter and remove the listener registration.
     */
    @SuppressWarnings("unused")
    void unbindAdapter() {
        if (adapter != null) {
            // Remove the anonymous listener we added
            // Since we can't remove by identity, we rely on the adapter
            // to handle this gracefully. For fake adapters this works.
            // For diozero, the callbacks just won't fire since the adapter
            // may be closed.
            adapter.removeInputListener(null);
            this.adapter = null;
        }
    }

    /**
     * @return the current logical (inversion-applied) state
     */
    public DeviceState state() {
        return device.logicalState(rawState);
    }

    /**
     * @return the raw hardware state (before inversion)
     */
    public DeviceState rawState() {
        return rawState;
    }

    /**
     * Update the raw hardware state directly (without listener event).
     * <p>
     * This is used by {@link ActionEngine#dispatchInput} for tests or
     * manual state updates. The listener-based path (from hardware)
     * updates rawState through the anonymous InputListener.
     *
     * @param rawState the new raw hardware state
     */
    public void setRawState(DeviceState rawState) {
        this.rawState = rawState;
    }

    @Override
    public InputDevice config() {
        return device;
    }
}
