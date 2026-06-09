package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Switch device — generates click events that can trigger actions on other devices.
 *
 * <pre>
 * # Legacy format
 * - id: switch_entrance
 *   display_name: "Entrance switch"
 *   click_action: light_kitchen
 *
 * # Profile-based format (SBC-009)
 * - id: klatka_switch
 *   name: Klatka 1
 *   input: input_board.input1
 *   location: floor1.stairs
 *   clicks: { click: true, double: true, long: false, release: false }
 *   actions:
 *     click:
 *       - { type: output.toggle, target: klatka_light }
 *   expose:
 *     ha: { enabled: true, events: [click, double] }
 * </pre>
 */
public final class SwitchDevice extends DeviceConfig {

    /**
     * Legacy: optional ID of a device to toggle when the switch is clicked.
     */
    @JsonProperty("click_action")
    private String clickAction;

    /**
     * Configured click detection settings.
     */
    @JsonProperty("clicks")
    private ClicksConfig clicks;

    /**
     * Named action handlers — maps event name to a list of action mappings.
     */
    @JsonProperty("actions")
    private java.util.Map<String, java.util.List<ActionMapping>> actions;

    public SwitchDevice() {}

    @JsonCreator
    public SwitchDevice(
            @JsonProperty("id") String id,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("click_action") String clickAction) {
        this.id = id;
        this.displayName = displayName;
        this.clickAction = clickAction;
    }

    public SwitchDevice(
            String id,
            String displayName,
            String clickAction,
            ClicksConfig clicks,
            java.util.Map<String, java.util.List<ActionMapping>> actions) {
        this.id = id;
        this.displayName = displayName;
        this.clickAction = clickAction;
        this.clicks = clicks;
        this.actions = actions;
    }

    @Override
    public DeviceType type() {
        return DeviceType.SWITCH;
    }

    public String clickAction() {
        return clickAction;
    }

    public ClicksConfig clicks() {
        return clicks;
    }

    public java.util.Map<String, java.util.List<ActionMapping>> actions() {
        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SwitchDevice that = (SwitchDevice) o;
        return Objects.equals(clickAction, that.clickAction)
                && Objects.equals(clicks, that.clicks)
                && Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clickAction, clicks, actions);
    }

    @Override
    public String toString() {
        return "SwitchDevice{id='" + id + "', displayName='" + displayName
                + "', clickAction='" + clickAction + "', clicks=" + clicks + ", actions=" + actions + "}";
    }
}
