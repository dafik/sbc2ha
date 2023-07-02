package com.dfi.sbc2ha.config.boneio.definition.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BoneIoThermSensorConfig extends BoneIoSensorConfig {
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement = "°C";

    public BoneIoThermSensorConfig() {
        super();
    }
}
