package iot.sbc2ha.hardware.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.PhysicalChannel;

import java.util.List;
import java.util.Objects;

/**
 * Represents a hardware profile — a named, reusable template of physical channels
 * and logical-to-physical mappings.
 * <p>
 * Profiles are loaded from YAML resources and expanded into a
 * {@link iot.sbc2ha.hardware.HardwareModel} via {@link ProfileRegistry}.
 * <p>
 * Profiles marked as incomplete are placeholders and should not be used in production.
 *
 * <pre>
 * id: boneio.input-v0.3
 * incomplete: true
 * channels:
 *   - type: gpio
 *     location: "P9_11"
 *     direction: input
 * mappings:
 *   - logical_id: switch_1
 *     description: "Input 1"
 *     physical:
 *       type: gpio
 *       location: "P9_11"
 *       direction: input
 * </pre>
 */
public final class HardwareProfile {

    private final String id;
    private final boolean incomplete;
    private final List<PhysicalChannel> channels;
    private final List<HardwareMapping> mappings;

    @SuppressWarnings("unused")
    HardwareProfile() {
        this.id = null;
        this.incomplete = false;
        this.channels = List.of();
        this.mappings = List.of();
    }

    @JsonCreator
    public HardwareProfile(
            @JsonProperty("id") String id,
            @JsonProperty("incomplete") Boolean incomplete,
            @JsonProperty("channels") List<PhysicalChannel> channels,
            @JsonProperty("mappings") List<HardwareMapping> mappings) {
        this.id = id;
        this.incomplete = incomplete != null && incomplete;
        this.channels = channels != null ? List.copyOf(channels) : List.of();
        this.mappings = mappings != null ? List.copyOf(mappings) : List.of();
    }

    /**
     * Unique profile identifier (e.g. "boneio.input-v0.3").
     */
    public String id() {
        return id;
    }

    /**
     * Whether this profile is an incomplete placeholder.
     * <p>
     * Incomplete profiles should not be used in production configurations.
     */
    public boolean incomplete() {
        return incomplete;
    }

    /**
     * Physical channels defined by this profile.
     */
    public List<PhysicalChannel> channels() {
        return channels;
    }

    /**
     * Logical-to-physical mappings defined by this profile.
     */
    public List<HardwareMapping> mappings() {
        return mappings;
    }

    /**
     * Number of channels in this profile.
     */
    public int channelCount() {
        return channels.size();
    }

    /**
     * Number of mappings in this profile.
     */
    public int mappingCount() {
        return mappings.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HardwareProfile that = (HardwareProfile) o;
        return incomplete == that.incomplete
                && Objects.equals(id, that.id)
                && Objects.equals(channels, that.channels)
                && Objects.equals(mappings, that.mappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, incomplete, channels, mappings);
    }

    @Override
    public String toString() {
        return "HardwareProfile{id='" + id + "', incomplete=" + incomplete
                + ", channels=" + channels.size() + ", mappings=" + mappings.size() + "}";
    }
}
