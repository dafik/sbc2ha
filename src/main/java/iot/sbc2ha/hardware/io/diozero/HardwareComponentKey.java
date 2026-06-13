package iot.sbc2ha.hardware.io.diozero;

import java.util.Objects;

/**
 * Composite key for {@link HardwareComponentRegistry}.
 *
 * <p>Identifies a hardware component by its type and instance identifier.
 * For example:</p>
 * <ul>
 *   <li>{@code type="mcp23017", id="i2c-2:0x20"} — one MCP23017 on I2C bus 2, addr 0x20</li>
 *   <li>{@code type="pca9685", id="i2c-2:0x40"} — one PCA9685 on I2C bus 2, addr 0x40</li>
 *   <li>{@code type="oled", id="i2c-1:0x3c"} — one OLED display on I2C bus 1, addr 0x3c</li>
 * </ul>
 *
 * <p>Implements {@code equals}/ {@code hashCode} based on type+id,
 * so it works correctly as a map key.</p>
 */
public final class HardwareComponentKey {

    private final String type;
    private final String id;
    private final int hashCode;

    /**
     * Create a key from type and id strings.
     *
     * @param type component type (e.g. "mcp23017", "pca9685", "oled")
     * @param id   instance identifier (unique within the type)
     */
    public HardwareComponentKey(String type, String id) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.hashCode = Objects.hash(type, id);
    }

    /**
     * Component type (e.g. "mcp23017").
     */
    public String type() {
        return type;
    }

    /**
     * Instance identifier (e.g. "0x20", "mcp0", "i2c-2:0x20").
     */
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HardwareComponentKey that = (HardwareComponentKey) o;
        return type.equals(that.type) && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return type + ":" + id;
    }
}
