package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Hardware profile reference — nested under {@code hardware.profiles.<id>:}.
 *
 * <pre>
 * profiles:
 *   input_board: { type: boneio.input-v0.4, board: main }
 *   output_board:
 *     type: boneio.output-24x16a-v0.4
 *     bus: i2c2
 *     addresses: { mcp1: 0x20, mcp2: 0x21 }
 * </pre>
 */
public final class ProfileConfig {

    @JsonProperty("type")
    private String type;

    @JsonProperty("board")
    private String board;

    @JsonProperty("bus")
    private String bus;

    @JsonProperty("addresses")
    private Map<String, String> addresses = new HashMap<>();

    public ProfileConfig() {
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String board() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String bus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }

    public Map<String, String> addresses() {
        return addresses;
    }

    public void setAddresses(Map<String, String> addresses) {
        this.addresses = addresses;
    }
}
