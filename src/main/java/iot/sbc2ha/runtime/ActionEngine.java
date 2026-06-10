package iot.sbc2ha.runtime;

import iot.sbc2ha.device.ActionMapping;
import iot.sbc2ha.device.DeviceConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.SwitchDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Wire switch runtimes (legacy clickAction + new actions format)
        for (DeviceConfig dev : registry.all()) {
            if (dev instanceof SwitchDevice switch1) {
                ActionType action = ActionType.NOOP;
                String targetId = switch1.clickAction();
                if (targetId != null && !targetId.isBlank()) {
                    action = ActionType.OUTPUT_TOGGLE;
                } else if (switch1.actions() != null && switch1.actions().containsKey("click")) {
                    var clickActions = switch1.actions().get("click");
                    if (clickActions != null && !clickActions.isEmpty()) {
                        var mapping = clickActions.getFirst();
                        action = mapActionType(mapping.type());
                        targetId = mapping.target();
                    }
                }
                SwitchRuntime runtime = new SwitchRuntime(switch1, action, targetId);
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
     * @param switchRuntime the switch runtime that received the click
     */
    public void dispatchClick(SwitchRuntime switchRuntime) {
        String targetId = switchRuntime.targetId();
        if (targetId == null || targetId.isBlank()) {
            log.debug("Switch '{}' has no click target — skipping", switchRuntime.id());
            return;
        }

        DeviceRuntime target = targetMap.get(targetId);
        if (target == null) {
            log.error("Switch '{}' click targets unknown device '{}'",
                    switchRuntime.id(), targetId);
            return;
        }

        ActionType action = switchRuntime.action();
        switch (action) {
            case OUTPUT_TOGGLE -> {
                if (target instanceof OutputRuntime out) {
                    out.toggle();
                    persist(out.id(), out.state());
                    log.info("Switch '{}' toggled output '{}' → {}",
                            switchRuntime.id(), targetId, out.state());
                } else if (target instanceof LightRuntime light) {
                    light.toggle();
                    persist(light.id(), light.state());
                    log.info("Switch '{}' toggled light '{}' → {}",
                            switchRuntime.id(), targetId, light.state());
                }
            }
            case OUTPUT_ON -> {
                if (target instanceof OutputRuntime out) {
                    out.setState(DeviceState.ON);
                    persist(out.id(), out.state());
                    log.info("Switch '{}' set output '{}' ON", switchRuntime.id(), targetId);
                } else if (target instanceof LightRuntime light) {
                    light.setState(DeviceState.ON);
                    persist(light.id(), light.state());
                    log.info("Switch '{}' set light '{}' ON", switchRuntime.id(), targetId);
                }
            }
            case OUTPUT_OFF -> {
                if (target instanceof OutputRuntime out) {
                    out.setState(DeviceState.OFF);
                    persist(out.id(), out.state());
                    log.info("Switch '{}' set output '{}' OFF", switchRuntime.id(), targetId);
                } else if (target instanceof LightRuntime light) {
                    light.setState(DeviceState.OFF);
                    persist(light.id(), light.state());
                    log.info("Switch '{}' set light '{}' OFF", switchRuntime.id(), targetId);
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
