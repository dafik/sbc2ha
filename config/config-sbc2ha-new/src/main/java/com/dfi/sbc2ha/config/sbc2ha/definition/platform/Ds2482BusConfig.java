package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class Ds2482BusConfig extends I2cBusConfig {
    public Ds2482BusConfig() {
        super();
        platform = PlatformType.DS2482;
    }
}
