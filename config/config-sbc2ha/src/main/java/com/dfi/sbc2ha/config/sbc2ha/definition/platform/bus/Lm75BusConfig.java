package com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class Lm75BusConfig extends I2cBusConfig {
    public Lm75BusConfig() {
        super();
        setPlatform(PlatformType.LM75);
    }
}
