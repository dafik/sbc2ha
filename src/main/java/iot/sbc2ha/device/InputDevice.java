package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import iot.sbc2ha.runtime.DeviceState;

import java.util.Objects;

/**
 * Binary input device — a sensor that reports on/off state (door, motion, contact).
 *
 * <pre>
 * - type: input
 *   id: door_entrance
 *   display_name: "Entrance door"
 *   input: input_board.input1
 *   location: floor1.entrance
 *   sensor_type: door
 *   inverted: false
 *   expose:
 *     ha: { enabled: true }
 * </pre>
 */
public final class InputDevice extends DeviceConfig {

    /**
     * Type of binary sensor this device represents.
     */
    @JsonProperty("sensor_type")
    private SensorType sensorType;

    /**
     * When true, the reported state is logically inverted:
     * hardware ON → state OFF, hardware OFF → state ON.
     */
    @JsonProperty("inverted")
    private Boolean inverted;

    @SuppressWarnings("unused")
    public InputDevice() {}

    @JsonCreator
    public InputDevice(
            @JsonProperty("id") String id,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("sensor_type") SensorType sensorType,
            @JsonProperty("inverted") Boolean inverted) {
        this.id = id;
        this.displayName = displayName;
        this.sensorType = sensorType;
        this.inverted = inverted;
    }

    public InputDevice(
            String id,
            String displayName,
            SensorType sensorType) {
        this.id = id;
        this.displayName = displayName;
        this.sensorType = sensorType;
        this.inverted = false;
    }

    @Override
    public DeviceType type() {
        return DeviceType.INPUT;
    }

    public SensorType sensorType() {
        return sensorType;
    }

    public Boolean inverted() {
        return inverted != null && inverted;
    }

    /**
     * The logical state: applies inversion to the raw hardware state.
     *
     * @param rawState the raw hardware state (ON = sensor triggered)
     * @return the logical state after inversion is applied
     */
    public DeviceState logicalState(DeviceState rawState) {
        if (inverted()) {
            return rawState == DeviceState.ON ? DeviceState.OFF : DeviceState.ON;
        }
        return rawState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InputDevice that = (InputDevice) o;
        return sensorType == that.sensorType
                && Objects.equals(inverted, that.inverted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensorType, inverted);
    }

    @Override
    public String toString() {
        return "InputDevice{id='" + id + "', displayName='" + displayName
                + "', sensorType=" + sensorType + ", inverted=" + inverted + "}";
    }

    /**
     * The type of binary sensor.
     */
    public enum SensorType {
        /** Door/window open/close sensor. */
        DOOR,
        /** Motion detection sensor. */
        MOTION,
        /** Contact sensor (same as door, generic). */
        CONTACT
    }
}
