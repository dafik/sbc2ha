package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DS18B20busDS2482 extends DS18B20 {


    public DS18B20busDS2482() {
        super();
        platform = PlatformType.DS2482;
    }
}
