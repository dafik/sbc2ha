package com.dfi.sbc2ha.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.diozero.devices.PCA9685;

public class PCA9685Bus extends Bus<PCA9685> {
    public PCA9685Bus(PCA9685 bus, String id) {
        super(PlatformType.PCA9685, bus, id);
    }
}
