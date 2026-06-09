package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Complete hardware mapping model for a board.
 * <p>
 * Contains all physical channel definitions and logical-to-physical mappings.
 * Provides resolution from logical device ID to the underlying physical channel.
 * <p>
 * Validates that every device in the registry has a corresponding mapping.
 *
 * <pre>
 * board: bone1
 * channels:
 *   - type: gpio
 *     location: "P9_11"
 *     direction: input
 *   - type: mcp23017
 *     location: "i2c-1:0x20:A:0"
 *     port: A
 *     pin: 0
 * mappings:
 *   - logical_id: btn_entrance
 *     description: "Entrance door button"
 *     physical:
 *       type: gpio
 *       location: "P9_11"
 *       direction: input
 *   - logical_id: out_relay1
 *     description: "First relay"
 *     physical:
 *       type: mcp23017
 *       location: "i2c-1:0x20:A:0"
 *       port: A
 *       pin: 0
 * </pre>
 */
public final class HardwareModel {

    private String board;
    private String profile;
    private List<PhysicalChannel> channels = new ArrayList<>();
    private List<HardwareMapping> mappings = new ArrayList<>();
    private final Map<String, HardwareMapping> byLogicalId = new LinkedHashMap<>();

    @SuppressWarnings("unused")
    HardwareModel() {}

    @JsonCreator
    public HardwareModel(
            @JsonProperty("board") String board,
            @JsonProperty("channels") List<PhysicalChannel> channels,
            @JsonProperty("mappings") List<HardwareMapping> mappings) {
        this.board = board;
        this.channels = channels != null ? List.copyOf(channels) : List.of();
        if (mappings != null) {
            this.mappings = new ArrayList<>(mappings);
            for (HardwareMapping m : this.mappings) {
                byLogicalId.put(m.logicalId(), m);
            }
        } else {
            this.mappings = List.of();
        }
    }

    /**
     * Create a HardwareModel with an optional profile reference.
     */
    public HardwareModel(String board, String profile,
                         List<PhysicalChannel> channels,
                         List<HardwareMapping> mappings) {
        this.board = board;
        this.profile = profile;
        this.channels = channels != null ? List.copyOf(channels) : List.of();
        if (mappings != null) {
            this.mappings = new ArrayList<>(mappings);
            for (HardwareMapping m : this.mappings) {
                byLogicalId.put(m.logicalId(), m);
            }
        } else {
            this.mappings = List.of();
        }
    }

    /**
     * Board profile name (e.g. "bone1").
     */
    public String board() {
        return board;
    }

    /**
     * Reference to the hardware profile this model was expanded from,
     * or {@code null} if created manually.
     */
    public String profile() {
        return profile;
    }

    /**
     * All registered physical channels, in insertion order.
     */
    public List<PhysicalChannel> channels() {
        return Collections.unmodifiableList(channels);
    }

    /**
     * All logical-to-physical mappings, in insertion order.
     */
    public List<HardwareMapping> mappings() {
        return Collections.unmodifiableList(mappings);
    }

    /**
     * Resolve a logical device ID to its hardware mapping.
     *
     * @param logicalId the logical device ID
     * @return the hardware mapping, or {@code null} if not found
     */
    public HardwareMapping getMapping(String logicalId) {
        return byLogicalId.get(logicalId);
    }

    /**
     * Resolve a logical device ID to its physical channel.
     *
     * @param logicalId the logical device ID
     * @return the physical channel, or {@code null} if not found
     */
    public PhysicalChannel getPhysicalChannel(String logicalId) {
        HardwareMapping mapping = byLogicalId.get(logicalId);
        if (mapping != null) {
            return mapping.physical();
        }
        return null;
    }

    /**
     * Resolve a logical device ID to its physical channel.
     * Throws {@link HardwareMappingException} if the logical ID has no mapping.
     *
     * @param logicalId the logical device ID
     * @return the resolved physical channel
     * @throws HardwareMappingException if no mapping exists for the logical ID
     */
    public PhysicalChannel resolve(String logicalId) {
        PhysicalChannel ch = getPhysicalChannel(logicalId);
        if (ch == null) {
            throw new HardwareMappingException(
                    "No hardware mapping for logical device: " + logicalId);
        }
        return ch;
    }

    /**
     * Validate that every device in the registry has a corresponding hardware mapping.
     *
     * @param deviceIds set of logical device IDs to check
     * @throws HardwareMappingException if any device has no mapping
     */
    public void validate(Set<String> deviceIds) {
        List<String> missing = deviceIds.stream()
                .filter(id -> !byLogicalId.containsKey(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new HardwareMappingException(
                    "Missing hardware mappings for devices: " + missing);
        }
    }

    /**
     * Number of mappings.
     */
    public int mappingCount() {
        return mappings.size();
    }

    /**
     * Number of physical channels.
     */
    public int channelCount() {
        return channels.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HardwareModel that = (HardwareModel) o;
        return Objects.equals(board, that.board)
                && Objects.equals(profile, that.profile)
                && Objects.equals(channels, that.channels)
                && Objects.equals(mappings, that.mappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, profile, channels, mappings);
    }

    @Override
    public String toString() {
        return "HardwareModel{board='" + board + "', channels=" + channels.size()
                + ", mappings=" + mappings.size() + "}";
    }
}
