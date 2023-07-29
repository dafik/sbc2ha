package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;

public class DS18B20busFs extends DS18B20 {


    public DS18B20busFs() {
        super();
        platform = PlatformType.DALLAS;
    }
}
