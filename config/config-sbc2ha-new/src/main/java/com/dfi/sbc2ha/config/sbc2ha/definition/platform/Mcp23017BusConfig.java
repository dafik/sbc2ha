package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Mcp23017BusConfig extends I2cBusConfig {
    public Mcp23017BusConfig() {
        super();
        platform = PlatformType.MCP23017;
    }
}
