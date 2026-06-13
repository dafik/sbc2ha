package iot.sbc2ha.hardware.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import iot.sbc2ha.hardware.HardwareChip;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.PhysicalChannel;

import java.util.List;
import java.util.Objects;

/**
 * Represents a hardware profile — a named, reusable template of I2C chip
 * declarations, physical channels, and logical-to-physical mappings.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class HardwareProfile {

    private final String id;
    private final boolean incomplete;
    private final List<HardwareChip> chips;
    private final List<PhysicalChannel> channels;
    private final List<HardwareMapping> mappings;

    @SuppressWarnings("unused")
    HardwareProfile() {
        this.id = null;
        this.incomplete = false;
        this.chips = List.of();
        this.channels = List.of();
        this.mappings = List.of();
    }

    @JsonCreator
    public HardwareProfile(
            @JsonProperty("id") String id,
            @JsonProperty("incomplete") Boolean incomplete,
            @JsonProperty("chips") List<HardwareChip> chips,
            @JsonProperty("channels") List<PhysicalChannel> channels,
            @JsonProperty("mappings") List<HardwareMapping> mappings) {
        this.id = id;
        this.incomplete = incomplete != null && incomplete;
        this.chips = chips != null ? List.copyOf(chips) : List.of();
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
     * I2C chips declared in this profile. Empty list if none.
     */
    public List<HardwareChip> chips() {
        return chips;
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
                && Objects.equals(chips, that.chips)
                && Objects.equals(channels, that.channels)
                && Objects.equals(mappings, that.mappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, incomplete, chips, channels, mappings);
    }

    @Override
    public String toString() {
        return "HardwareProfile{id='" + id + "', incomplete=" + incomplete
                + ", chips=" + chips.size()
                + ", channels=" + channels.size() + ", mappings=" + mappings.size() + "}";
    }
}
