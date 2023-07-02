package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class AdcSensorConfig extends ValueSensorConfig {


    int analog;

    public AdcSensorConfig() {
        super();
        platform = PlatformType.ANALOG;
    }
}
