package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Output device — a physical output pin / relay that can be toggled.
 *
 * <pre>
 * - id: out_relay1
 *   display_name: "Relay 1"
 * </pre>
 */
public final class OutputDevice extends DeviceConfig {

    public OutputDevice() {}

    @JsonCreator
    public OutputDevice(
            @JsonProperty("id") String id,
            @JsonProperty("display_name") String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public DeviceType type() {
        return DeviceType.OUTPUT;
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
        return "OutputDevice{id='" + id + "', displayName='" + displayName + "'}";
    }
}
