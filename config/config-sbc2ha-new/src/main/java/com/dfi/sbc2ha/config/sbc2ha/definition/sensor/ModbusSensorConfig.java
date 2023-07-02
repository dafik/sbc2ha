package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class ModbusSensorConfig extends ValueSensorConfig {

    int address;
    String model;

    public ModbusSensorConfig() {
        platform = PlatformType.MODBUS;
    }
}
