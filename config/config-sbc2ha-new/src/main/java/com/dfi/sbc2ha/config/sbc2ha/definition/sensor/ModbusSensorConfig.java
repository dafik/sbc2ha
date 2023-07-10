package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ModbusSensorConfig extends BusSensorConfig {

    int address;
    String model;

    public ModbusSensorConfig() {
        super();
        platform = PlatformType.MODBUS;
    }
}
