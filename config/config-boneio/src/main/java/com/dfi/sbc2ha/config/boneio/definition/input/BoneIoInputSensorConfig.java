package com.dfi.sbc2ha.config.boneio.definition.input;


import com.dfi.sbc2ha.config.boneio.definition.enums.InputKindType;
import com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha.BoneIoBinarySensorDeviceClassType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoInputSensorConfig extends BoneIoInputConfig<BoneIoInputSensorAction> {

    @JsonProperty("device_class")
    BoneIoBinarySensorDeviceClassType deviceClass;

    public BoneIoInputSensorConfig() {
        super();
        kind = InputKindType.SENSOR;
    }
}
