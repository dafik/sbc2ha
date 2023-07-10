package com.dfi.sbc2ha.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.modbus.Modbus;

public class ModbusBus extends Bus<Modbus> {
    public ModbusBus(Modbus bus, String id) {
        super(PlatformType.MODBUS, bus, id);
    }
}
