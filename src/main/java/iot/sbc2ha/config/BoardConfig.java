package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Board specification — nested under {@code hardware.board:}.
 */
public final class BoardConfig {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    public BoardConfig() {
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
