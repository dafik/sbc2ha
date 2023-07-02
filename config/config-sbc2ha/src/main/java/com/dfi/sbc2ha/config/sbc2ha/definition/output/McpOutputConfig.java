package com.dfi.sbc2ha.config.sbc2ha.definition.output;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputKindType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class McpOutputConfig extends OutputConfig {

    @JsonProperty("mcp_id")
    String mcpId;

    public McpOutputConfig() {
        super();
        kind = OutputKindType.MCP;
    }
}
