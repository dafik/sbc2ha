package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DS18B20busFs extends DS18B20 {


    public DS18B20busFs() {
        super();
        platform = PlatformType.DALLAS;
    }
}
