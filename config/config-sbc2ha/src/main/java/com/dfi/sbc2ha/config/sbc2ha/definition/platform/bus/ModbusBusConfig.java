package com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModbusBusConfig extends BusConfig {
    public static final String BUS_ID = "modbus";

    UartType uart;
    @JsonProperty("use_diozero")
    boolean useDiozero = true;

    public ModbusBusConfig() {
        busId = BUS_ID;
        setPlatform(PlatformType.MODBUS);
    }
}
