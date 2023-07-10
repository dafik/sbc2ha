package com.dfi.sbc2ha.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.diozero.api.I2CDevice;

public class Lm75Bus extends I2cBus {
    public Lm75Bus(I2CDevice bus, String id) {
        super(PlatformType.LM75, bus, id);
    }
}
