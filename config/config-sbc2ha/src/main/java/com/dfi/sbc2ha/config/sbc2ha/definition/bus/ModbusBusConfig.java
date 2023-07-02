package com.dfi.sbc2ha.config.sbc2ha.definition.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import lombok.Data;

@Data
public class ModbusBusConfig {

    public static final String BUS_ID = "modbus";
    String id = BUS_ID;
    UartType uart;
}
