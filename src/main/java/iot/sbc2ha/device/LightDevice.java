package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Light device — a togglable output that can be referenced by button click actions.
 *
 * <pre>
 * # Legacy format
 * - id: light_kitchen
 *   display_name: "Kitchen light"
 *
 * # Profile-based format (SBC-009)
 * - id: klatka_light
 *   name: Klatka
 *   output: output_board.output1
 *   location: floor1.stairs
 *   restore_state: true
 *   expose:
 *     ha: { enabled: true }
 * </pre>
 */
public final class LightDevice extends DeviceConfig {

    /**
     * Restore last state on startup.
     */
    @JsonProperty("restore_state")
    private Boolean restoreState;

    public LightDevice() {}

    @JsonCreator
    public LightDevice(
            @JsonProperty("id") String id,
            @JsonProperty("display_name") String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public LightDevice(
            String id,
            String displayName,
            Boolean restoreState) {
        this.id = id;
        this.displayName = displayName;
        this.restoreState = restoreState;
    }

    @Override
    public DeviceType type() {
        return DeviceType.LIGHT;
    }

    public Boolean restoreState() {
        return restoreState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LightDevice that = (LightDevice) o;
        return Objects.equals(restoreState, that.restoreState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), restoreState);
    }

    @Override
    public String toString() {
        return "LightDevice{id='" + id + "', displayName='" + displayName + "', restoreState=" + restoreState + "}";
    }
}
