package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.InputKindType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SwitchDeviceClassType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InputSwitchConfig extends InputConfig<InputSwitchAction> {

    @JsonProperty("device_class")
    SwitchDeviceClassType deviceClass;

    public InputSwitchConfig() {
        super();
        kind = InputKindType.SWITCH;
    }
}
