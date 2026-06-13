package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MCP23017 I2C GPIO expander channel.
 *
 * <p>References a chip by {@code bus} id (declared in the profile's
 * {@code chips:} section) and a global pin number 0-15.</p>
 *
 * <pre>
 * type: mcp23017
 * bus: mcp1
 * pin: 10
 * </pre>
 * <p>
 * Global pin 0-7 maps to PORT A, 8-15 maps to PORT B.
 * The chip's I2C address/bus are resolved from the referenced chip.
 * </p>
 */
public final class Mcp23017Channel extends PhysicalChannel {

    private int pin;

    public Mcp23017Channel() {}

    @JsonCreator
    public Mcp23017Channel(
            @JsonProperty("bus") String bus,
            @JsonProperty("pin") int pin) {
        super(bus);
        if (pin < 0 || pin > 15) {
            throw new IllegalArgumentException("MCP23017 pin must be 0-15, got " + pin);
        }
        this.pin = pin;
    }

    /**
     * Global pin number on the MCP23017 chip (0-15).
     * 0-7 = PORT A, 8-15 = PORT B.
     */
    @Override
    public int pin() {
        return pin;
    }

    /**
     * PORT A pin number (0-7), or -1 if pin is on PORT B.
     */
    public int portAPin() {
        return pin >= 0 && pin <= 7 ? pin : -1;
    }

    /**
     * PORT B pin number (0-7), or -1 if pin is on PORT A.
     */
    public int portBPin() {
        return pin >= 8 && pin <= 15 ? pin - 8 : -1;
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.MCP23017;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Mcp23017Channel that = (Mcp23017Channel) o;
        return pin == that.pin;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), pin);
    }

    @Override
    public String toString() {
        return "Mcp23017Channel{bus='" + bus() + "', pin=" + pin + "}";
    }
}
