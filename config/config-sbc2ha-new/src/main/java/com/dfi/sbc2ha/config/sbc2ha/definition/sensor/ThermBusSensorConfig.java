package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ThermBusSensorConfig extends BusSensorConfig {
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement = "°C";

    public ThermBusSensorConfig() {
        super();
    }
}
