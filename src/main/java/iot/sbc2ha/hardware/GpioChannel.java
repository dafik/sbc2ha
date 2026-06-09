package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Direct BBB GPIO pin channel.
 *
 * <p>Example location: {@code "P9_11"} (BeagleBone Black header pin).</p>
 *
 * <pre>
 * type: gpio
 * location: "P9_11"
 * direction: input
 * </pre>
 */
public final class GpioChannel extends PhysicalChannel {

    private Direction direction;

    public GpioChannel() {}

    @JsonCreator
    public GpioChannel(
            @JsonProperty("location") String location,
            @JsonProperty("direction") Direction direction) {
        super(location);
        this.direction = direction;
    }

    public Direction direction() {
        return direction;
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
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), direction);
    }

    @Override
    public String toString() {
        return "GpioChannel{location='" + location() + "', direction=" + direction + "}";
    }

    public enum Direction {
        INPUT,
        OUTPUT
    }
}
