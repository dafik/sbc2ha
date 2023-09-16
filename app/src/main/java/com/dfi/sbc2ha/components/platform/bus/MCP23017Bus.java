package com.dfi.sbc2ha.components.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.diozero.devices.MCP23017;

public class MCP23017Bus extends Bus<MCP23017> {
    public MCP23017Bus(MCP23017 bus, String id) {
        super(PlatformType.MCP23017, bus, id);
    }
}
