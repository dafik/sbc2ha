package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Complete hardware mapping model for a board.
 * <p>
 * Contains I2C chip declarations, physical channel definitions, and
 * logical-to-physical mappings. I2C channels reference chips by {@code bus} id.
 * Provides resolution from logical device ID to the underlying physical channel.
 * <p>
 * Validates that every device in the registry has a corresponding mapping.
 *
 * <pre>
 * board: bone1
 * chips:
 *   - id: mcp1
 *     type: mcp23017
 *     i2c_address: 0x20
 * channels:
 *   - type: mcp23017
 *     bus: mcp1
 *     pin: 0
 * mappings:
 *   - logical_id: out_relay1
 *     description: "First relay"
 *     physical:
 *       type: mcp23017
 *       bus: mcp1
 *       pin: 0
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class HardwareModel {

    private String board;
    private String profile;
    private List<HardwareChip> chips;
    private List<PhysicalChannel> channels = new ArrayList<>();
    private List<HardwareMapping> mappings = new ArrayList<>();
    private final Map<String, HardwareMapping> byLogicalId = new LinkedHashMap<>();
    private final Map<String, HardwareChip> chipsById = new LinkedHashMap<>();

    @SuppressWarnings("unused")
    HardwareModel() {}

    /**
     * Create a HardwareModel with an optional profile reference.
     */
    @JsonCreator
    public HardwareModel(
            @JsonProperty("board") String board,
            @JsonProperty("profile") String profile,
            @JsonProperty("chips") List<HardwareChip> chips,
            @JsonProperty("channels") List<PhysicalChannel> channels,
            @JsonProperty("mappings") List<HardwareMapping> mappings) {
        this.board = board;
        this.profile = profile;
        if (chips != null) {
            this.chips = List.copyOf(chips);
            for (HardwareChip c : this.chips) {
                chipsById.put(c.id(), c);
            }
        } else {
            this.chips = List.of();
        }
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
     * I2C chips declared in this profile, in insertion order.
     * Empty list if no chips are declared.
     */
    public List<HardwareChip> chips() {
        return Collections.unmodifiableList(chips);
    }

    /**
     * Lookup a chip by its bus id.
     *
     * @param id the chip id (e.g. "mcp1", "oled1")
     * @return the chip, or {@code null} if not found
     */
    public HardwareChip getChip(String id) {
        return chipsById.get(id);
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
                && Objects.equals(chips, that.chips)
                && Objects.equals(channels, that.channels)
                && Objects.equals(mappings, that.mappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, profile, chips, channels, mappings);
    }

    @Override
    public String toString() {
        return "HardwareModel{board='" + board + "', chips=" + chips.size()
                + ", channels=" + channels.size()
                + ", mappings=" + mappings.size() + "}";
    }
}
