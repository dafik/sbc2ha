package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusSensorConfig extends BusSensorConfig {

    int address;
    String model;

    public ModbusSensorConfig() {
        super();
        platform = PlatformType.MODBUS;
    }


}
