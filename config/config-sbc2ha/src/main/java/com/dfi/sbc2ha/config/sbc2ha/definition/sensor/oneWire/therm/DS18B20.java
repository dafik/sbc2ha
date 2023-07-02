package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class DS18B20 extends OneWireTherm {

    public DS18B20() {
        super();
        platform = PlatformType.DS2482;
    }
}
