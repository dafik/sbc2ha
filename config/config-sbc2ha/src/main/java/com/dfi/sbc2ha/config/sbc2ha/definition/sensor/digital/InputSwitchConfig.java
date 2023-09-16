package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ButtonState;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SwitchDeviceClassType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputSwitchConfig extends InputConfig<InputSwitchAction> {

    @JsonProperty("click_detection")
    private ButtonState clickDetection = ButtonState.SINGLE;

    @JsonProperty("device_class")
    private SwitchDeviceClassType deviceClass;

    public InputSwitchConfig() {
        super(PlatformType.SWITCH);
    }
}
