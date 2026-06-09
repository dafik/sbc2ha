package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Maps a logical device ID to a physical hardware channel.
 * <p>
 * This is the bridge between the device configuration layer
 * ({@link iot.sbc2ha.device.DeviceConfig}) and the physical hardware layer.
 *
 * <pre>
 * logical_id: switch_entrance
 * description: "Entrance door switch on P9_11"
 * physical:
 *   type: gpio
 *   location: "P9_11"
 *   direction: input
 * </pre>
 */
public final class HardwareMapping {

    private String logicalId;
    private String description;
    private PhysicalChannel physical;

    @SuppressWarnings("unused")
    HardwareMapping() {}

    @JsonCreator
    public HardwareMapping(
            @JsonProperty("logical_id") String logicalId,
            @JsonProperty("description") String description,
            @JsonProperty("physical") PhysicalChannel physical) {
        this.logicalId = logicalId;
        this.description = description;
        this.physical = physical;
    }

    /**
     * Logical device ID from the device configuration.
     */
    public String logicalId() {
        return logicalId;
    }

    /**
     * Human-readable description of this mapping.
     */
    public String description() {
        return description;
    }

    /**
     * Resolved physical hardware channel.
     */
    public PhysicalChannel physical() {
        return physical;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HardwareMapping that = (HardwareMapping) o;
        return Objects.equals(logicalId, that.logicalId)
                && Objects.equals(description, that.description)
                && Objects.equals(physical, that.physical);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicalId, description, physical);
    }

    @Override
    public String toString() {
        return "HardwareMapping{logicalId='" + logicalId + "', description='" + description
                + "', physical=" + physical + "}";
    }
}
