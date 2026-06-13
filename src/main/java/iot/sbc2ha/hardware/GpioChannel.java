package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Direct BBB GPIO pin channel.
 *
 * <p>References a board pin directly — no bus abstraction.</p>
 *
 * <pre>
 * type: gpio
 * pin: P9_11
 * direction: input
 * </pre>
 */
public final class GpioChannel extends PhysicalChannel {

    private String pin;
    private Direction direction;

    public GpioChannel() {}

    @JsonCreator
    public GpioChannel(
            @JsonProperty("pin") String pin,
            @JsonProperty("direction") Direction direction) {
        super(null);
        this.pin = pin;
        this.direction = direction;
    }

    /**
     * Board pin reference (e.g. "P9_11").
     */
    public String pinLabel() {
        return pin;
    }

    /**
     * Numeric pin value derived from the board pin (e.g. 11 for P9_11).
     */
    @Override
    public int pin() {
        int lastUnderscore = pin.lastIndexOf('_');
        if (lastUnderscore >= 0) {
            try { return Integer.parseInt(pin.substring(lastUnderscore + 1)); }
            catch (NumberFormatException e) { return -1; }
        }
        try { return Integer.parseInt(pin); }
        catch (NumberFormatException e) { return -1; }
    }

    public Direction direction() {
        return direction;
    }

    @Override
    public String location() {
        return pinLabel();
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.GPIO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GpioChannel that = (GpioChannel) o;
        return direction == that.direction
                && java.util.Objects.equals(pin, that.pin);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), pin, direction);
    }

    @Override
    public String toString() {
        return "GpioChannel{pin='" + pin + "', direction=" + direction + "}";
    }

    public enum Direction {
        INPUT,
        OUTPUT
    }
}
