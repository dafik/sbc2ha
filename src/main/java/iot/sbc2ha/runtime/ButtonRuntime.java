package iot.sbc2ha.runtime;

import iot.sbc2ha.device.ButtonDevice;

/**
 * Runtime wrapper for a {@link ButtonDevice}.
 *
 * <p>Exposes the configured {@link ActionType} and target device ID.
 * The {@link ActionEngine} wires buttons to their targets during
 * initialization and dispatches clicks externally.</p>
 */
public final class ButtonRuntime extends DeviceRuntime {

    private final ActionType action;
    private final String targetId;

    /**
     * Creates a button runtime with no action (noop).
     */
    public ButtonRuntime(ButtonDevice device) {
        super(device);
        this.action = ActionType.NOOP;
        this.targetId = null;
    }

    /**
     * Creates a button runtime wired to the given action and target.
     *
     * @param device   the underlying button device
     * @param action   the action to dispatch on click
     * @param targetId the stable ID of the target device
     */
    public ButtonRuntime(ButtonDevice device, ActionType action, String targetId) {
        super(device);
        this.action = action;
        this.targetId = targetId;
    }

    /**
     * Dispatch the configured action to the target device.
     *
     * <p>The caller (typically an {@link ActionType#OUTPUT_TOGGLE engine})
     * uses {@link #action()} and {@link #targetId()} to perform the dispatch.</p>
     *
     * @return the configured {@link ActionType}
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
