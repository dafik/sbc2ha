package com.dfi.sbc2ha.config.boneio.definition.bus;


import com.dfi.sbc2ha.config.boneio.definition.enums.BoneIoUartType;
import lombok.Data;

@Data
public class BoneIoModbusBusConfig {

    public static final String BUS_ID = "modbus";
    String id = BUS_ID;
    BoneIoUartType uart;
}
