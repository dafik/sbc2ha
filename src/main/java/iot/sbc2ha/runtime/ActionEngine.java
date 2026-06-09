package iot.sbc2ha.runtime;

import iot.sbc2ha.device.ButtonDevice;
import iot.sbc2ha.device.DeviceConfig;
import iot.sbc2ha.device.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Engine that dispatches button events to target devices.
 *
 * <p>Construction wires every {@link ButtonDevice} in the registry to a
 * {@link ButtonRuntime} using the button's {@code click_action} mapping.
 * Toggle actions are resolved against a map of {@link DeviceRuntime} targets
 * (outputs and lights).</p>
 *
 * <h3>Dispatch flow</h3>
 * <pre>
 * button.click() → engine.dispatchClick(buttonRuntime)
 *                 → target = runtimes.get(buttonRuntime.targetId())
 *                 → target.toggle()
 * </pre>
 */
public final class ActionEngine {

    private static final Logger log = LoggerFactory.getLogger(ActionEngine.class);

    private final Map<String, ButtonRuntime> buttonMap = new LinkedHashMap<>();
    private final Map<String, DeviceRuntime> targetMap = new LinkedHashMap<>();

    /**
     * Creates an action engine from a device registry, building all button
     * runtimes and resolving target references.
     *
     * @param registry the validated device registry
     */
    public ActionEngine(DeviceRegistry registry) {
        // Index all togglable targets first (outputs + lights)
        for (DeviceConfig dev : registry.all()) {
            if (dev instanceof iot.sbc2ha.device.OutputDevice) {
                targetMap.put(dev.id(), new OutputRuntime((iot.sbc2ha.device.OutputDevice) dev));
            } else if (dev instanceof iot.sbc2ha.device.LightDevice) {
                targetMap.put(dev.id(), new LightRuntime((iot.sbc2ha.device.LightDevice) dev));
            }
        }

        // Wire button runtimes
        for (DeviceConfig dev : registry.all()) {
            if (dev instanceof ButtonDevice btn) {
                ActionType action = ActionType.NOOP;
                String targetId = btn.clickAction();
                if (targetId != null && !targetId.isBlank()) {
                    action = ActionType.OUTPUT_TOGGLE;
                }
                ButtonRuntime runtime = new ButtonRuntime(btn, action, targetId);
                buttonMap.put(dev.id(), runtime);
                log.info("Wired button '{}' → action={} target={}",
                        dev.id(), action, targetId);
            }
        }

        log.info("ActionEngine initialized: {} buttons, {} togglable targets",
                buttonMap.size(), targetMap.size());
    }

    /**
     * @return all button runtimes keyed by device ID
     */
    public Map<String, ButtonRuntime> buttons() {
        return buttonMap;
    }

    /**
     * @return all togglable targets keyed by device ID
     */
    public Map<String, DeviceRuntime> targets() {
        return targetMap;
    }

    /**
     * Dispatch a click event from the given button runtime to its target.
     *
     * @param buttonRuntime the button runtime that received the click
     */
    public void dispatchClick(ButtonRuntime buttonRuntime) {
        String targetId = buttonRuntime.targetId();
        if (targetId == null || targetId.isBlank()) {
            log.debug("Button '{}' has no click target — skipping", buttonRuntime.id());
            return;
        }

        DeviceRuntime target = targetMap.get(targetId);
        if (target == null) {
            log.error("Button '{}' click targets unknown device '{}'",
                    buttonRuntime.id(), targetId);
            return;
        }

        ActionType action = buttonRuntime.action();
        switch (action) {
            case OUTPUT_TOGGLE -> {
                if (target instanceof OutputRuntime out) {
                    out.toggle();
                    log.info("Button '{}' toggled output '{}' → {}",
                            buttonRuntime.id(), targetId, out.state());
                } else if (target instanceof LightRuntime light) {
                    light.toggle();
                    log.info("Button '{}' toggled light '{}' → {}",
                            buttonRuntime.id(), targetId, light.state());
                }
            }
            case OUTPUT_ON -> {
                if (target instanceof OutputRuntime out) {
                    out.setState(DeviceState.ON);
                    log.info("Button '{}' set output '{}' ON", buttonRuntime.id(), targetId);
                } else if (target instanceof LightRuntime light) {
                    light.setState(DeviceState.ON);
                    log.info("Button '{}' set light '{}' ON", buttonRuntime.id(), targetId);
                }
            }
            case OUTPUT_OFF -> {
                if (target instanceof OutputRuntime out) {
                    out.setState(DeviceState.OFF);
                    log.info("Button '{}' set output '{}' OFF", buttonRuntime.id(), targetId);
                } else if (target instanceof LightRuntime light) {
                    light.setState(DeviceState.OFF);
                    log.info("Button '{}' set light '{}' OFF", buttonRuntime.id(), targetId);
                }
            }
            case NOOP -> {
                // nothing to do
            }
        }
    }

    /**
     * Get a button runtime by device ID.
     *
     * @param deviceId the stable device ID
     * @return the button runtime, or {@code null} if not found
     */
    public ButtonRuntime getButton(String deviceId) {
        return buttonMap.get(deviceId);
    }

    /**
     * Get a target runtime (output or light) by device ID.
     *
     * @param deviceId the stable device ID
     * @return the target runtime, or {@code null} if not found
     */
    public DeviceRuntime getTarget(String deviceId) {
        return targetMap.get(deviceId);
    }
}
