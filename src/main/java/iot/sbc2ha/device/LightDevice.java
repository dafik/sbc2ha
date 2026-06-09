package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Light device — a togglable output that can be referenced by button click actions.
 *
 * <pre>
 * - id: light_kitchen
 *   display_name: "Kitchen light"
 * </pre>
 */
public final class LightDevice extends DeviceConfig {

    public LightDevice() {}

    @JsonCreator
    public LightDevice(
            @JsonProperty("id") String id,
            @JsonProperty("display_name") String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public DeviceType type() {
        return DeviceType.LIGHT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "LightDevice{id='" + id + "', displayName='" + displayName + "'}";
    }
}
