package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.BinarySensorDeviceClassType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputSensorConfig extends InputConfig<InputSensorAction> {

    @JsonProperty("device_class")
    BinarySensorDeviceClassType deviceClass;

    public InputSensorConfig() {
        super(PlatformType.DIGITAL);
    }
}
