package iot.sbc2ha.runtime;

import iot.sbc2ha.device.SwitchDevice;

/**
 * Runtime wrapper for a {@link SwitchDevice}.
 *
 * <p>Exposes the configured {@link ActionType} and target device ID.
 * The {@link ActionEngine} wires switchs to their targets during
 * initialization and dispatches clicks externally.</p>
 */
public final class SwitchRuntime extends DeviceRuntime {

    private final ActionType action;
    private final String targetId;

    /**
     * Creates a switch runtime with no action (noop).
     */
    public SwitchRuntime(SwitchDevice device) {
        super(device);
        this.action = ActionType.NOOP;
        this.targetId = null;
    }

    /**
     * Creates a switch runtime wired to the given action and target.
     *
     * @param device   the underlying switch device
     * @param action   the action to dispatch on click
     * @param targetId the stable ID of the target device
     */
    public SwitchRuntime(SwitchDevice device, ActionType action, String targetId) {
        super(device);
        this.action = action;
        this.targetId = targetId;
    }

    /**
     * Dispatch the configured action to the target device.
     *
     * <p>The caller (typically an {@link ActionEngine})
     * uses action() and {@link #targetId()} to perform the dispatch.</p>
     *
     * @return the configured action (e.g. {@code OUTPUT_TOGGLE}, {@code NOOP})
     */
    public ActionType action() {
        return action;
    }

    /**
     * @return the stable ID of the target device, or {@code null} if no target is set
     */
    public String targetId() {
        return targetId;
    }
}
