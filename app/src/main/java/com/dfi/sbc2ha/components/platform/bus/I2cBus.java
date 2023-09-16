package com.dfi.sbc2ha.components.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.diozero.api.I2CDevice;

public abstract class I2cBus extends Bus<I2CDevice> {
    public I2cBus(PlatformType platformType, I2CDevice bus, String id) {
        super(platformType, bus, id);
    }
}
