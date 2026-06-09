package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Home Assistant exposure settings for a device.
 *
 * <pre>
 * expose:
 *   ha: { enabled: true, events: [click, double] }
 * </pre>
 */
@SuppressWarnings("unused")
public final class HaExposeConfig {

    private final boolean enabled;
    private final List<String> events;

    @JsonCreator
    public HaExposeConfig(
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("events") List<String> events) {
        this.enabled = enabled;
        this.events = events != null ? events : List.of();
    }

    public HaExposeConfig() {
        this(false, List.of());
    }

    public boolean enabled() {
        return enabled;
    }

    public List<String> events() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HaExposeConfig that = (HaExposeConfig) o;
        return enabled == that.enabled && Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, events);
    }

    @Override
    public String toString() {
        return "HaExposeConfig{enabled=" + enabled + ", events=" + events + "}";
    }
}
