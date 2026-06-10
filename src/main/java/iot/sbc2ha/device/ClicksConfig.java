package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Configures which switch click events to detect.
 *
 * <pre>
 * clicks: { click: true, double: true, long: false, release: false, debounceMs: 30 }
 * </pre>
 */
public final class ClicksConfig {

    private static final int DEFAULT_DEBOUNCE_MS = 30;

    private final boolean click;
    private final boolean dbl;
    private final boolean longPress;
    private final boolean release;
    private final int debounceMs;

    @JsonCreator
    public ClicksConfig(
            @JsonProperty("click") boolean click,
            @JsonProperty("double") boolean dbl,
            @JsonProperty("long") boolean longPress,
            @JsonProperty("release") boolean release,
            @JsonProperty("debounceMs") int debounceMs) {
        this.click = click;
        this.dbl = dbl;
        this.longPress = longPress;
        this.release = release;
        this.debounceMs = debounceMs > 0 ? debounceMs : DEFAULT_DEBOUNCE_MS;
    }

    /**
     * Legacy constructor — delegates to the full constructor with default debounce.
     * Used by code that constructs ClicksConfig programmatically (not via Jackson).
     */
    public ClicksConfig(
            boolean click,
            boolean dbl,
            boolean longPress,
            boolean release) {
        this(click, dbl, longPress, release, DEFAULT_DEBOUNCE_MS);
    }

    public ClicksConfig() {
        this(true, false, false, false);
    }

    public boolean click() {
        return click;
    }

    public boolean dbl() {
        return dbl;
    }

    public boolean longPress() {
        return longPress;
    }

    public boolean release() {
        return release;
    }

    /**
     * Debounce window in milliseconds for mechanical switch bounce filtering.
     * <p>Zero or negative values are clamped to the default of 30ms.</p>
     *
     * @return debounce window in milliseconds (minimum 30)
     */
    public int debounceMs() {
        return debounceMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClicksConfig that = (ClicksConfig) o;
        return click == that.click
                && dbl == that.dbl
                && longPress == that.longPress
                && release == that.release
                && debounceMs == that.debounceMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(click, dbl, longPress, release, debounceMs);
    }

    @Override
    public String toString() {
        return "ClicksConfig{click=" + click + ", dbl=" + dbl + ", longPress=" + longPress
                + ", release=" + release + ", debounceMs=" + debounceMs + "}";
    }
}
