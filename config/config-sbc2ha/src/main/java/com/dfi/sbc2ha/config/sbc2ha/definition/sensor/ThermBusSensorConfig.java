package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ThermBusSensorConfig extends BusSensorConfig {
    @JsonProperty("unit_of_measurement")
    private String unitOfMeasurement = "°C";

    public ThermBusSensorConfig() {
        super();
    }

}
