package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Configures which button click events to detect.
 *
 * <pre>
 * clicks: { click: true, double: true, long: false, release: false }
 * </pre>
 */
public final class ClicksConfig {

    private final boolean click;
    private final boolean dbl;
    private final boolean longPress;
    private final boolean release;

    @JsonCreator
    public ClicksConfig(
            @JsonProperty("click") boolean click,
            @JsonProperty("double") boolean dbl,
            @JsonProperty("long") boolean longPress,
            @JsonProperty("release") boolean release) {
        this.click = click;
        this.dbl = dbl;
        this.longPress = longPress;
        this.release = release;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClicksConfig that = (ClicksConfig) o;
        return click == that.click
                && dbl == that.dbl
                && longPress == that.longPress
                && release == that.release;
    }

    @Override
    public int hashCode() {
        return Objects.hash(click, dbl, longPress, release);
    }

    @Override
    public String toString() {
        return "ClicksConfig{click=" + click + ", dbl=" + dbl + ", longPress=" + longPress + ", release=" + release + "}";
    }
}
