package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ScheduledSensorConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalogSensorConfig extends ScheduledSensorConfig {


    int analog;

    public AnalogSensorConfig() {
        super();
        platform = PlatformType.ANALOG;
    }
}
