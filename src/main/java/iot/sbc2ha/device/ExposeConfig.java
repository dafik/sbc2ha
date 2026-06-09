package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Top-level expose wrapper — maps to the {@code expose:} YAML key.
 *
 * <pre>
 * expose:
 *   ha: { enabled: true, events: [click, double] }
 * </pre>
 */
@SuppressWarnings("unused")
public final class ExposeConfig {

    @JsonProperty("ha")
    private HaExposeConfig ha;

    public ExposeConfig() {
    }

    public ExposeConfig(HaExposeConfig ha) {
        this.ha = ha;
    }

    public HaExposeConfig ha() {
        return ha;
    }

    public void setHa(HaExposeConfig ha) {
        this.ha = ha;
    }
}
