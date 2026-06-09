package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Represents a physical hardware channel on the board.
 * <p>
 * Subtypes model different bus/technologies (GPIO, I2C expander, etc.).
 * Jackson polymorphic deserialization uses {@code type} property.
 */
@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GpioChannel.class, name = "gpio"),
        @JsonSubTypes.Type(value = Mcp23017Channel.class, name = "mcp23017")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class PhysicalChannel {

    /**
     * Board-bus-module path identifying the channel's physical location.
     * E.g. "P9_11" for a direct BBB GPIO pin, "i2c-1:42:0" for MCP23017 port A pin 0.
     */
    private String location;

    protected PhysicalChannel() {}

    public PhysicalChannel(String location) {
        this.location = location;
    }

    public String location() {
        return location;
    }

    public abstract ChannelType channelType();

    public enum ChannelType {
        GPIO,
        MCP23017
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalChannel that = (PhysicalChannel) o;
        return java.util.Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(location);
    }

    @Override
    public String toString() {
        return "PhysicalChannel{location='" + location + "'}";
    }
}
