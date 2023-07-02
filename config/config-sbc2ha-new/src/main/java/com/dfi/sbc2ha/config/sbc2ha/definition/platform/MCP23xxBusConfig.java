package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class MCP23xxBusConfig extends I2cBusConfig {

    public MCP23xxBusConfig() {
        platform = PlatformType.LM75;
    }
}
