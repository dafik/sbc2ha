package iot.sbc2ha.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Button device — generates click events that can trigger actions on other devices.
 *
 * <pre>
 * - id: btn_entrance
 *   display_name: "Entrance button"
 *   click_action: light_kitchen  # optional target device ID
 * </pre>
 */
public final class ButtonDevice extends DeviceConfig {

    /**
     * Optional ID of a device to toggle when the button is clicked.
     * Must reference an existing {@link DeviceConfig} (validated by {@link DeviceRegistry}).
     */
    @JsonProperty("click_action")
    private String clickAction;

    public ButtonDevice() {}

    @JsonCreator
    public ButtonDevice(
            @JsonProperty("id") String id,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("click_action") String clickAction) {
        this.id = id;
        this.displayName = displayName;
        this.clickAction = clickAction;
    }

    @Override
    public DeviceType type() {
        return DeviceType.BUTTON;
    }

    public String clickAction() {
        return clickAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ButtonDevice that = (ButtonDevice) o;
        return Objects.equals(clickAction, that.clickAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clickAction);
    }

    @Override
    public String toString() {
        return "ButtonDevice{id='" + id + "', displayName='" + displayName + "', clickAction='" + clickAction + "'}";
    }
}
