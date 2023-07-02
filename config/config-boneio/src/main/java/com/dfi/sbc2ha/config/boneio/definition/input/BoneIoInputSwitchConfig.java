package com.dfi.sbc2ha.config.boneio.definition.input;


import com.dfi.sbc2ha.config.boneio.definition.enums.InputKindType;
import com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha.BoneIoSwitchDeviceClassType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoInputSwitchConfig extends BoneIoInputConfig<BoneIoInputSwitchAction> {

    @JsonProperty("device_class")
    BoneIoSwitchDeviceClassType deviceClass;

    public BoneIoInputSwitchConfig() {
        super();
        kind = InputKindType.SWITCH;
    }
}
