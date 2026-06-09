package iot.sbc2ha.device;

import iot.sbc2ha.config.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry that holds all devices and validates:
 * <ul>
 *   <li>Each device ID is unique.</li>
 *   <li>Every {@code click_action} target references an existing device.</li>
 * </ul>
 */
public final class DeviceRegistry {

    private final List<DeviceConfig> devices = new ArrayList<>();
    private final Map<String, DeviceConfig> byId = new LinkedHashMap<>();

    public DeviceRegistry() {}

    /**
     * Add a device to the registry.
     */
    public void add(DeviceConfig device) {
        Objects.requireNonNull(device, "device must not be null");
        Objects.requireNonNull(device.id(), "device id must not be null");
        devices.add(device);
        byId.put(device.id(), device);
    }

    /**
     * Validate the registry:
     * <ul>
     *   <li>No duplicate IDs (shouldn't happen after add, but safe to check).</li>
     *   <li>Every button click_action target exists as a device.</li>
     * </ul>
     *
     * @throws ValidationException if validation fails
     */
    public void validate() {
        Set<String> seenIds = new HashSet<>();
        for (DeviceConfig dev : devices) {
            if (!seenIds.add(dev.id())) {
                throw new ValidationException("Duplicate device ID: " + dev.id());
            }
        }
        for (DeviceConfig dev : devices) {
            if (dev instanceof ButtonDevice btn) {
                String target = btn.clickAction();
                if (target != null && !target.isBlank()) {
                    if (!byId.containsKey(target)) {
                        throw new ValidationException(
                                "Button '" + dev.id() + "' click_action targets unknown device: " + target);
                    }
                }
            }
        }
    }

    /**
     * Look up a device by stable ID.
     *
     * @return the device, or {@code null} if not found
     */
    public DeviceConfig getById(String id) {
        return byId.get(id);
    }

    /**
     * All registered devices, in insertion order.
     */
    public List<DeviceConfig> all() {
        return Collections.unmodifiableList(devices);
    }

    /**
     * All button devices.
     */
    public List<ButtonDevice> buttons() {
        return devices.stream()
                .filter(d -> d instanceof ButtonDevice)
                .map(d -> (ButtonDevice) d)
                .collect(Collectors.toList());
    }

    /**
     * All light devices.
     */
    public List<LightDevice> lights() {
        return devices.stream()
                .filter(d -> d instanceof LightDevice)
                .map(d -> (LightDevice) d)
                .collect(Collectors.toList());
    }

    /**
     * All output devices.
     */
    public List<OutputDevice> outputs() {
        return devices.stream()
                .filter(d -> d instanceof OutputDevice)
                .map(d -> (OutputDevice) d)
                .collect(Collectors.toList());
    }

    /**
     * Number of devices.
     */
    public int size() {
        return devices.size();
    }
}
