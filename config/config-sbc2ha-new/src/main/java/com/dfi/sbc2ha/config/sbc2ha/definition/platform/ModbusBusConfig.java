package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import lombok.Data;

@Data
public class ModbusBusConfig extends PlatformConfig {

    public static final String BUS_ID = "modbus";
    UartType uart;

    public ModbusBusConfig() {
        id = BUS_ID;
        platform = PlatformType.MODBUS;
    }
}
