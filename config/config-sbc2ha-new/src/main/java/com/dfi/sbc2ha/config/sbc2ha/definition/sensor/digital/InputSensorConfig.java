package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.InputKindType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.BinarySensorDeviceClassType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InputSensorConfig extends InputConfig<InputSensorAction> {

    @JsonProperty("device_class")
    BinarySensorDeviceClassType deviceClass;

    public InputSensorConfig() {
        super();
        kind = InputKindType.SENSOR;
    }
}
