package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ThermSensorConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class OneWireTherm extends ThermSensorConfig {
    String address;

    public OneWireTherm() {
    }
}
