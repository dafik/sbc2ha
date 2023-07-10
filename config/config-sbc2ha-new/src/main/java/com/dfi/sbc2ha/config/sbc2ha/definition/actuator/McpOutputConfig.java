package com.dfi.sbc2ha.config.sbc2ha.definition.actuator;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputKindType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class McpOutputConfig extends ActuatorConfig {

    public McpOutputConfig() {
        super();
        kind = OutputKindType.MCP;
    }
}
