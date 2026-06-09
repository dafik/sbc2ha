package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Hardware configuration — top-level {@code hardware:} key.
 *
 * <pre>
 * hardware:
 *   board: { id: main, type: beaglebone-black }
 *   buses:
 *     i2c2: { type: i2c, path: /dev/i2c-2 }
 *   profiles:
 *     input_board: { type: boneio.input-v0.4, board: main }
 *     output_board:
 *       type: boneio.output-24x16a-v0.4
 *       bus: i2c2
 *       addresses: { mcp1: 0x20, mcp2: 0x21 }
 * </pre>
 */
public final class HardwareConfig {

    @JsonProperty("board")
    private BoardConfig board;

    @JsonProperty("buses")
    private Map<String, BusConfig> buses = new HashMap<>();

    @JsonProperty("profiles")
    private Map<String, ProfileConfig> profiles = new HashMap<>();

    public HardwareConfig() {
    }

    public BoardConfig board() {
        return board;
    }

    public void setBoard(BoardConfig board) {
        this.board = board;
    }

    public Map<String, BusConfig> buses() {
        return buses;
    }

    public void setBuses(Map<String, BusConfig> buses) {
        this.buses = buses;
    }

    public Map<String, ProfileConfig> profiles() {
        return profiles;
    }

    public void setProfiles(Map<String, ProfileConfig> profiles) {
        this.profiles = profiles;
    }
}
