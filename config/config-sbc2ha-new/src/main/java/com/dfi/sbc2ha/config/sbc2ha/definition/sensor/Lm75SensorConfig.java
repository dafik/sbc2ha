package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Lm75SensorConfig extends ThermBusSensorConfig {

    public Lm75SensorConfig() {
        super();
        platform = PlatformType.LM75;
    }
}
