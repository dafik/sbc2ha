package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MCP23017 I2C GPIO expander channel.
 *
 * <p>Location encodes I2C bus, device address, port and pin index.</p>
 *
 * <pre>
 * type: mcp23017
 * location: "i2c-1:0x20:A:0"
 * </pre>
 * <p>
 * Location format: {@code "<i2c-bus>:<address>:<port>:<pin>"}
 * where port is {@code "A"} or {@code "B"}, pin is 0-7.
 * </p>
 */
public final class Mcp23017Channel extends PhysicalChannel {

    private Port port;
    private int pin;

    public Mcp23017Channel() {}

    @JsonCreator
    public Mcp23017Channel(
            @JsonProperty("location") String location,
            @JsonProperty("port") Port port,
            @JsonProperty("pin") int pin) {
        super(location);
        this.port = port;
        this.pin = pin;
    }

    /**
     * I2C address as integer (e.g. 0x20 = 32).
     * Derivable from location; convenience accessor for programmatic construction.
     * Supports both hex (0x20) and decimal (32) formats.
     */
    public int i2cAddress() {
        // Parse from location string: "i2c-1:0x20:A:0" or "i2c-1:32:A:0" -> 32
        String[] parts = location().split(":");
        if (parts.length >= 2) {
            String addrStr = parts[1];
            if (addrStr.startsWith("0x") || addrStr.startsWith("0X")) {
                return Integer.parseUnsignedInt(addrStr.substring(2), 16);
            } else {
                try {
                    return Integer.parseInt(addrStr);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public Port port() {
        return port;
    }

    public int pin() {
        return pin;
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
        return pin == that.pin && port == that.port;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), port, pin);
    }

    @Override
    public String toString() {
        return "Mcp23017Channel{location='" + location() + "', port=" + port + ", pin=" + pin + "}";
    }

    public enum Port {
        A,
        B
    }
}
