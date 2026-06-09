package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Location specification — nested under {@code locations:}.
 */
public final class LocationConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("parent")
    private String parent;

    public LocationConfig() {
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String parent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
