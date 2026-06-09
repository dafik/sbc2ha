package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Maps a named action to a target device and action type.
 *
 * <pre>
 * actions:
 *   click:
 *     - { type: output.toggle, target: klatka_light }
 * </pre>
 */
public final class ActionMapping {

    private final ActionType type;
    private String target;

    @JsonCreator
    public ActionMapping(
            @JsonProperty("type") ActionType type,
            @JsonProperty("target") String target) {
        this.type = type;
        this.target = target;
    }

    public ActionMapping() {
        this.type = null;
    }

    public ActionType type() {
        return type;
    }

    public String target() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionMapping that = (ActionMapping) o;
        return type == that.type && Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target);
    }

    @Override
    public String toString() {
        return "ActionMapping{type=" + type + ", target='" + target + "'}";
    }

    public enum ActionType {
        @JsonProperty("output.toggle")
        OUTPUT_TOGGLE,
        @JsonProperty("output.on")
        OUTPUT_ON,
        @JsonProperty("output.off")
        OUTPUT_OFF
    }
}
