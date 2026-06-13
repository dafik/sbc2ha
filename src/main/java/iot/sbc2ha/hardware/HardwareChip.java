package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Declares an I2C chip in a hardware profile.
 *
 * <p>Chips are referenced by channels via a {@code bus} id. The profile
 * owns the I2C address/bus knowledge — channels only carry a bus id and pin.
 *
 * <pre>
 * chips:
 *   - id: mcp1
 *     type: mcp23017
 *     i2c_address: 0x20
 *   - id: oled1
 *     type: oled
 *     i2c_address: 0x3C
 * </pre>
 *
 * <p>Optional {@code i2c_bus} defaults to 2 (BBB I2C-2).</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class HardwareChip {

    private String id;
    private String type;
    private int i2cAddress;
    private Integer i2cBus;

    @SuppressWarnings("unused")
    HardwareChip() {}

    @JsonCreator
    public HardwareChip(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("i2c_address") int i2cAddress) {
        this.id = id;
        this.type = type;
        this.i2cAddress = i2cAddress;
    }

    /**
     * Bus id referenced by channels (e.g. "mcp1", "oled1").
     */
    public String id() {
        return id;
    }

    /**
     * Chip type (e.g. "mcp23017", "pca9685", "oled").
     */
    public String type() {
        return type;
    }

    /**
     * I2C device address (e.g. 0x20 = 32).
     */
    public int i2cAddress() {
        return i2cAddress;
    }

    /**
     * I2C bus number. Defaults to 2 if not specified.
     */
    @JsonProperty("i2c_bus")
    public int i2cBus() {
        return i2cBus != null ? i2cBus : 2;
    }

    @JsonProperty("i2c_bus")
    public void setI2cBus(Integer i2cBus) {
        this.i2cBus = i2cBus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HardwareChip that = (HardwareChip) o;
        return i2cAddress == that.i2cAddress
                && Objects.equals(id, that.id)
                && Objects.equals(type, that.type)
                && Objects.equals(i2cBus, that.i2cBus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, i2cAddress, i2cBus);
    }

    @Override
    public String toString() {
        return "HardwareChip{id='" + id + "', type='" + type
                + "', i2cAddress=0x" + Integer.toHexString(i2cAddress)
                + ", i2cBus=" + i2cBus() + "}";
    }
}
