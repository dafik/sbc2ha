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
 * <p>
 * I2C channels (MCP23017, PCA9685, OLED) reference a chip by {@code bus} id
 * — the chip is declared in the profile's {@code chips:} section.
 * GPIO channels have no bus — they reference board pins directly.
 */
@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GpioChannel.class, name = "gpio"),
        @JsonSubTypes.Type(value = Mcp23017Channel.class, name = "mcp23017"),
        @JsonSubTypes.Type(value = OledChannel.class, name = "oled")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class PhysicalChannel {

    /**
     * Bus id referencing a chip declared in the profile's {@code chips:} section.
     * Null for GPIO channels (no bus abstraction).
     */
    private String bus;

    protected PhysicalChannel() {}

    protected PhysicalChannel(String bus) {
        this.bus = bus;
    }

    /**
     * Bus id referencing a chip, or {@code null} for GPIO channels.
     */
    public String bus() {
        return bus;
    }

    public abstract ChannelType channelType();

    /**
     * Numeric pin number for this channel.
     * Returns -1 for channels that don't have a numeric pin (e.g. OLED displays).
     * For MCP23017 channels, returns the global pin number (0-15).
     * For GPIO channels, returns the numeric suffix of the board pin.
     */
    public int pin() {
        return -1;
    }

    /**
     * A human-readable location identifier for this channel.
     * For GPIO channels, returns the board pin (e.g. "P9_11").
     * For I2C channels (MCP23017, PCA9685, OLED), returns the bus id.
     */
    public String location() {
        return bus;
    }

    public enum ChannelType {
        GPIO,
        MCP23017,
        PCA9685,
        OLED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalChannel that = (PhysicalChannel) o;
        return java.util.Objects.equals(bus, that.bus);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(bus);
    }

    @Override
    public String toString() {
        return "PhysicalChannel{bus='" + bus + "'}";
    }
}
