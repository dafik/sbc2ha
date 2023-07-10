package com.dfi.sbc2ha.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.diozero.devices.oneWire.bus.OneWireBus;

public class OWireBus extends Bus<OneWireBus> {
    public OWireBus(PlatformType platformType, OneWireBus bus, String id) {
        super(platformType, bus, id);
    }
}
