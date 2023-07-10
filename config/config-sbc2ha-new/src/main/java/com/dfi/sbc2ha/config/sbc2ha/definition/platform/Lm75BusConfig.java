package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class Lm75BusConfig extends I2cBusConfig {
    public Lm75BusConfig() {
        super();
        platform = PlatformType.LM75;
    }
}
