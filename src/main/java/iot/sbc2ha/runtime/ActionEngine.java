package iot.sbc2ha.runtime;

import iot.sbc2ha.device.ActionMapping;
import iot.sbc2ha.device.DeviceConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.SwitchDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Engine that dispatches switch events to target devices.
 *
 * <p>Construction wires every {@link SwitchDevice} in the registry to a
 * {@link SwitchRuntime} using the switch's {@code click_action} mapping.
 * Toggle actions are resolved against a map of {@link DeviceRuntime} targets
 * (outputs and lights).</p>
 *
 * <h3>Dispatch flow</h3>
 * <pre>
 * switch.click() → engine.dispatchClick(switchRuntime)
 *                 → target = runtimes.get(switchRuntime.targetId())
 *                 → target.toggle()
 * </pre>
 */
public final class ActionEngine {

    private static final Logger log = LoggerFactory.getLogger(ActionEngine.class);

    private final Map<String, SwitchRuntime> switchMap = new LinkedHashMap<>();
    private final Map<String, DeviceRuntime> targetMap = new LinkedHashMap<>();
    private final StateService stateService;

    /**
     * Creates an action engine from a device registry, building all switch
     * runtimes and resolving target references. No state restoration is performed.
     *
     * @param registry the validated device registry
     */
    public ActionEngine(DeviceRegistry registry) {
        this(registry, null);
    }

    /**
     * Creates an action engine from a device registry, building all switch
     * runtimes and resolving target references. If a {@code stateService} is
     * provided, persisted states are loaded and applied to all target runtimes.
     *
     * <p>Indexing order: outputs and lights first (for restore_state), then
     * input devices (binary sensors, transient — not restored from state).</p>
     *
     * @param registry     the validated device registry
     * @param stateService persistent state store, or {@code null} for no restore
     */
    public ActionEngine(DeviceRegistry registry, StateService stateService) {
        this.stateService = stateService;

        // Index all togglable targets first (outputs + lights)
        for (DeviceConfig dev : registry.all()) {
            if (dev instanceof iot.sbc2ha.device.OutputDevice) {
                targetMap.put(dev.id(), new OutputRuntime((iot.sbc2ha.device.OutputDevice) dev));
            } else if (dev instanceof iot.sbc2ha.device.LightDevice) {
                targetMap.put(dev.id(), new LightRuntime((iot.sbc2ha.device.LightDevice) dev));
            }
        }

        // Restore persisted states for togglable targets
        if (stateService != null) {
            Map<String, DeviceState> restored = stateService.load();
            for (var entry : restored.entrySet()) {
                DeviceRuntime target = targetMap.get(entry.getKey());
                if (target != null) {
                    if (target instanceof OutputRuntime out) {
                        out.setState(entry.getValue());
                    } else if (target instanceof LightRuntime light) {
                        light.setState(entry.getValue());
                    }
                    log.info("Restored state for {}: {}", entry.getKey(), entry.getValue());
                }
            }
        }

        // Index input devices (binary sensors) — transient, not restored
        for (DeviceConfig dev : registry.all()) {
            if (dev instanceof iot.sbc2ha.device.InputDevice inputDev) {
                targetMap.put(dev.id(), new InputRuntime(inputDev));
                log.info("Indexed input device '{}': type={}, inverted={}",
                        dev.id(), inputDev.sensorType(), inputDev.inverted());
            }
        }

        // Wire switch runtimes (legacy clickAction + new multi-event actions)
        for (DeviceConfig dev : registry.all()) {
            if (dev instanceof SwitchDevice switch1) {
                var eventActions = new EnumMap<EventType, ActionType>(EventType.class);
                var eventTargets = new EnumMap<EventType, String>(EventType.class);

                // Default: no-op for all events
                for (EventType et : EventType.values()) {
                    eventActions.put(et, ActionType.NOOP);
                    eventTargets.put(et, null);
                }

                // Legacy clickAction (wire as CLICK event)
                ActionType action = ActionType.NOOP;
                String targetId = switch1.clickAction();
                if (targetId != null && !targetId.isBlank()) {
                    action = ActionType.OUTPUT_TOGGLE;
                    eventActions.put(EventType.CLICK, ActionType.OUTPUT_TOGGLE);
                    eventTargets.put(EventType.CLICK, targetId);
                } else if (switch1.actions() != null) {
                    // Wire all configured event keys
                    for (var entry : switch1.actions().entrySet()) {
                        EventType eventType = eventTypeFromName(entry.getKey());
                        if (eventType == null) {
                            log.debug("Unknown event name '{}' for switch '{}', skipping",
                                    entry.getKey(), dev.id());
                            continue;
                        }
                        var mappings = entry.getValue();
                        if (mappings != null && !mappings.isEmpty()) {
                            var mapping = mappings.getFirst();
                            ActionType mappedAction = mapActionType(mapping.type());
                            String mappedTarget = mapping.target();
                            eventActions.put(eventType, mappedAction);
                            eventTargets.put(eventType, mappedTarget);
                            log.debug("Wired switch '{}' event '{}': action={} target={}",
                                    dev.id(), eventType, mappedAction, mappedTarget);
                        }
                        if (eventType == EventType.CLICK) {
                            action = eventActions.get(EventType.CLICK);
                            targetId = eventTargets.get(EventType.CLICK);
                        }
                    }
                }

                SwitchRuntime runtime = new SwitchRuntime(switch1, eventActions, eventTargets);
                switchMap.put(dev.id(), runtime);
                log.info("Wired switch '{}' → action={} target={}",
                        dev.id(), action, targetId);
            }
        }

        log.info("ActionEngine initialized: {} switchs, {} togglable targets",
                switchMap.size(), targetMap.size());
    }

    /**
     * Convert an {@link ActionMapping.ActionType} to a runtime {@link ActionType}.
     */
    private static ActionType mapActionType(ActionMapping.ActionType mappingType) {
        if (mappingType == null) return ActionType.NOOP;
        switch (mappingType) {
            case OUTPUT_TOGGLE -> {
                return ActionType.OUTPUT_TOGGLE;
            }
            case OUTPUT_ON -> {
                return ActionType.OUTPUT_ON;
            }
            case OUTPUT_OFF -> {
                return ActionType.OUTPUT_OFF;
            }
            default -> {
                return ActionType.NOOP;
            }
        }
    }

    /**
     * Convert a YAML event name string to an {@link EventType}.
     *
     * @param name the YAML key (e.g. "click", "double", "long", "release")
     * @return the corresponding EventType, or {@code null} if unrecognized
     */
    private static EventType eventTypeFromName(String name) {
        if (name == null) return null;
        return switch (name.toLowerCase()) {
            case "click" -> EventType.CLICK;
            case "double" -> EventType.DOUBLE;
            case "long" -> EventType.LONG;
            case "release" -> EventType.RELEASE;
            default -> null;
        };
    }

    /**
     * @return all switch runtimes keyed by device ID
     */
    public Map<String, SwitchRuntime> switchs() {
        return switchMap;
    }

    /**
     * @return all togglable targets keyed by device ID
     */
    public Map<String, DeviceRuntime> targets() {
        return targetMap;
    }

    /**
     * Dispatch a click event from the given switch runtime to its target.
     *
     * <p>Convenience method that delegates to {@link #dispatchEvent(SwitchRuntime, EventType)}
     * with {@link EventType#CLICK}.</p>
     *
     * @param switchRuntime the switch runtime that received the click
     */
    public void dispatchClick(SwitchRuntime switchRuntime) {
        dispatchEvent(switchRuntime, EventType.CLICK);
    }

    /**
     * Dispatch an event from the given switch runtime to its target device.
     *
     * <p>Looks up the action and target for the specific event type,
     * then performs the corresponding state change on the target.</p>
     *
     * @param switchRuntime the switch runtime that received the event
     * @param eventType     the event type that was detected
     */
    public void dispatchEvent(SwitchRuntime switchRuntime, EventType eventType) {
        ActionType action = switchRuntime.action(eventType);
        String targetId = switchRuntime.targetId(eventType);

        if (targetId == null || targetId.isBlank()) {
            log.debug("Switch '{}' has no {} target — skipping", switchRuntime.id(), eventType);
            return;
        }

        DeviceRuntime target = targetMap.get(targetId);
        if (target == null) {
            log.error("Switch '{}' {} targets unknown device '{}'",
                    switchRuntime.id(), eventType, targetId);
            return;
        }

        switch (action) {
            case OUTPUT_TOGGLE -> {
                if (target instanceof OutputRuntime out) {
                    out.toggle();
                    persist(out.id(), out.state());
                    log.info("Switch '{}' {} toggled output '{}' → {}",
                            switchRuntime.id(), eventType, targetId, out.state());
                } else if (target instanceof LightRuntime light) {
                    light.toggle();
                    persist(light.id(), light.state());
                    log.info("Switch '{}' {} toggled light '{}' → {}",
                            switchRuntime.id(), eventType, targetId, light.state());
                }
            }
            case OUTPUT_ON -> {
                if (target instanceof OutputRuntime out) {
                    out.setState(DeviceState.ON);
                    persist(out.id(), out.state());
                    log.info("Switch '{}' {} set output '{}' ON", switchRuntime.id(), eventType, targetId);
                } else if (target instanceof LightRuntime light) {
                    light.setState(DeviceState.ON);
                    persist(light.id(), light.state());
                    log.info("Switch '{}' {} set light '{}' ON", switchRuntime.id(), eventType, targetId);
                }
            }
            case OUTPUT_OFF -> {
                if (target instanceof OutputRuntime out) {
                    out.setState(DeviceState.OFF);
                    persist(out.id(), out.state());
                    log.info("Switch '{}' {} set output '{}' OFF", switchRuntime.id(), eventType, targetId);
                } else if (target instanceof LightRuntime light) {
                    light.setState(DeviceState.OFF);
                    persist(light.id(), light.state());
                    log.info("Switch '{}' {} set light '{}' OFF", switchRuntime.id(), eventType, targetId);
                }
            }
            case NOOP -> {
                // nothing to do
            }
        }
    }

    /**
     * Get a switch runtime by device ID.
     *
     * @param deviceId the stable device ID
     * @return the switch runtime, or {@code null} if not found
     */
    public SwitchRuntime getSwitch(String deviceId) {
        return switchMap.get(deviceId);
    }

    /**
     * Get a target runtime (output, light, or input) by device ID.
     *
     * @param deviceId the stable device ID
     * @return the target runtime, or {@code null} if not found
     */
    public DeviceRuntime getTarget(String deviceId) {
        return targetMap.get(deviceId);
    }

    /**
     * Get an input (binary sensor) runtime by device ID.
     *
     * @param deviceId the stable device ID
     * @return the input runtime, or {@code null} if not found or not an input device
     */
    public InputRuntime getInput(String deviceId) {
        DeviceRuntime target = targetMap.get(deviceId);
        if (target instanceof InputRuntime inputRuntime) {
            return inputRuntime;
        }
        return null;
    }

    /**
     * Simulate a hardware state change on a binary input device.
     * <p>
     * This is the primary method for the hardware layer (or fake runtime)
     * to notify the engine of a sensor state change. The engine tracks
     * the raw state and the logical state (with inversion applied) is
     * available via {@link InputRuntime#state()}.
     *
     * @param deviceId the stable device ID of the input device
     * @param rawState the new raw hardware state (ON = sensor triggered)
     * @return true if the device was found and state was updated
     */
    public boolean dispatchInput(String deviceId, DeviceState rawState) {
        InputRuntime input = getInput(deviceId);
        if (input == null) {
            log.warn("dispatchInput: unknown input device '{}'", deviceId);
            return false;
        }
        input.setRawState(rawState);
        log.debug("Input '{}' state changed: raw={} logical={}",
                deviceId, rawState, input.state());
        return true;
    }

    /**
     * Persist a state change through the StateService if available.
     */
    private void persist(String deviceId, DeviceState state) {
        if (stateService != null) {
            stateService.setState(deviceId, state);
        }
    }

    /**
     * @return the StateService used for state restoration, or {@code null} if none
     */
    public StateService stateService() {
        return stateService;
    }
}
