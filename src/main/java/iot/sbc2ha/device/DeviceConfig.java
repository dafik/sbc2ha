package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Base device configuration with stable ID — all devices share this contract.
 * <p>
 * Jackson polymorphic deserialization uses the {@code type} property to determine
 * the actual subtype (ButtonDevice, LightDevice, OutputDevice).
 */
@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ButtonDevice.class, name = "button"),
        @JsonSubTypes.Type(value = LightDevice.class, name = "light"),
        @JsonSubTypes.Type(value = OutputDevice.class, name = "output")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class DeviceConfig {

    /**
     * Stable, machine-readable device identifier.
     * Must match ^[a-z0-9][a-z0-9._-]*$ (enforced by {@link DeviceRegistry}).
     */
    @JsonProperty("id")
    protected String id;

    /**
     * Human-readable display name (mutable, never a persistence key).
     * Alias: {@code name} maps to the same value.
     */
    @JsonProperty("display_name")
    protected String displayName;

    /**
     * Human-readable display name — alias for {@code display_name}.
     * Jackson merges both into the same {@link #displayName} field.
     */
    @JsonProperty("name")
    protected String nameAlias;

    /**
     * Reference to a hardware channel (e.g. {@code input_board.input1}).
     */
    @JsonProperty("input")
    protected String input;

    /**
     * Reference to a hardware channel (e.g. {@code output_board.output1}).
     */
    @JsonProperty("output")
    protected String output;

    /**
     * Logical location path (e.g. {@code floor1.stairs}).
     */
    @JsonProperty("location")
    protected String location;

    /**
     * Home Assistant exposure configuration.
     */
    @JsonProperty("expose")
    protected ExposeConfig expose;

    protected DeviceConfig() {}

    public String id() { return id; }
    public String displayName() {
        if (displayName != null) return displayName;
        if (nameAlias != null) return nameAlias;
        return null;
    }
    public String name() { return displayName(); }

    public String input() { return input; }
    public String output() { return output; }
    public String location() { return location; }
    public ExposeConfig expose() { return expose; }

    public abstract DeviceType type();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceConfig that = (DeviceConfig) o;
        return Objects.equals(id, that.id)
                && Objects.equals(displayName, that.displayName)
                && type() == that.type();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, type());
    }

    @Override
    public String toString() {
        return "DeviceConfig{id='" + id + "', displayName='" + displayName + "'}";
    }

    public enum DeviceType {
        BUTTON,
        LIGHT,
        OUTPUT
    }
}
