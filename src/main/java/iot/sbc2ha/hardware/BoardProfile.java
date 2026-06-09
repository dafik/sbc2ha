package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Static metadata about a known board type.
 * <p>
 * Used as documentation/template for known BBB revisions and BoneIO board combinations.
 * Actual hardware mapping is manual — profiles do not auto-generate mappings.
 *
 * <p>
 * Example: "bone1" = BBB + MCP23017 on I2C-1 + SH1106 OLED on I2C-2.
 */
public final class BoardProfile {

    private String name;
    private String platform;
    private Map<String, String> description;

    @SuppressWarnings("unused")
    BoardProfile() {}

    @JsonCreator
    public BoardProfile(
            @JsonProperty("name") String name,
            @JsonProperty("platform") String platform,
            @JsonProperty("description") Map<String, String> description) {
        this.name = name;
        this.platform = platform;
        this.description = description != null ? Map.copyOf(description) : Collections.emptyMap();
    }

    /**
     * Profile name (e.g. "bone1").
     */
    public String name() {
        return name;
    }

    /**
     * SBC platform (e.g. "beaglebone-black").
     */
    public String platform() {
        return platform;
    }

    /**
     * Human-readable description keyed by key (e.g. "board", "notes").
     */
    public Map<String, String> description() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardProfile that = (BoardProfile) o;
        return Objects.equals(name, that.name)
                && Objects.equals(platform, that.platform)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, platform, description);
    }

    @Override
    public String toString() {
        return "BoardProfile{name='" + name + "', platform='" + platform + "'}";
    }
}
