package iot.sbc2ha.runtime;

import iot.sbc2ha.device.DeviceConfig;

/**
 * Base class for all runtime device wrappers.
 *
 * <p>Wraps a {@link DeviceConfig} and exposes its stable ID so the
 * {@link ActionType#OUTPUT_TOGGLE engine} can dispatch actions by target ID.</p>
 */
public abstract class DeviceRuntime {

    private final DeviceConfig config;

    protected DeviceRuntime(DeviceConfig config) {
        this.config = config;
    }

    /**
     * @return the underlying device configuration
     */
    public DeviceConfig config() {
        return config;
    }

    /**
     * Stable device ID — used as the dispatch key for actions.
     */
    public String id() {
        return config.id();
    }
}
