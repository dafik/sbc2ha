package iot.sbc2ha.runtime;

import iot.sbc2ha.device.SwitchDevice;

import java.util.EnumMap;
import java.util.Map;

/**
 * Runtime wrapper for a {@link SwitchDevice}.
 *
 * <p>Exposes the configured action and target device ID for each event type
 * (click, double, long, release). The {@link ActionEngine} wires switchs to
 * their targets during initialization and dispatches events externally.</p>
 */
public final class SwitchRuntime extends DeviceRuntime {

    /** Per-event actions, keyed by event type. */
    private final EnumMap<EventType, ActionType> eventActions;
    /** Per-event target IDs, keyed by event type. */
    private final EnumMap<EventType, String> eventTargets;

    /**
     * Creates a switch runtime with NOOP for all event types.
     */
    public SwitchRuntime(SwitchDevice device) {
        super(device);
        this.eventActions = new EnumMap<>(EventType.class);
        this.eventTargets = new EnumMap<>(EventType.class);
        for (EventType et : EventType.values()) {
            eventActions.put(et, ActionType.NOOP);
            eventTargets.put(et, null);
        }
    }

    /**
     * Creates a switch runtime wired to the given action and target for CLICK.
     *
     * @param device   the underlying switch device
     * @param action   the action to dispatch on click
     * @param targetId the stable ID of the target device
     */
    public SwitchRuntime(SwitchDevice device, ActionType action, String targetId) {
        super(device);
        this.eventActions = new EnumMap<>(EventType.class);
        this.eventTargets = new EnumMap<>(EventType.class);
        for (EventType et : EventType.values()) {
            eventActions.put(et, ActionType.NOOP);
            eventTargets.put(et, null);
        }
        eventActions.put(EventType.CLICK, action);
        eventTargets.put(EventType.CLICK, targetId);
    }

    /**
     * Creates a switch runtime with per-event actions.
     *
     * @param device       the underlying switch device
     * @param eventActions map of event type to action type
     * @param eventTargets map of event type to target device ID
     */
    public SwitchRuntime(SwitchDevice device,
                         Map<EventType, ActionType> eventActions,
                         Map<EventType, String> eventTargets) {
        super(device);
        this.eventActions = new EnumMap<>(eventActions);
        this.eventTargets = new EnumMap<>(eventTargets);
    }

    /**
     * @return the action for the given event type, or NOOP if not configured
     */
    public ActionType action(EventType eventType) {
        return eventActions.getOrDefault(eventType, ActionType.NOOP);
    }

    /**
     * @return the target device ID for the given event type, or {@code null} if not configured
     */
    public String targetId(EventType eventType) {
        return eventTargets.get(eventType);
    }
}
