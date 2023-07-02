package com.dfi.sbc2ha.config.boneio.definition.output;


import com.dfi.sbc2ha.config.boneio.definition.enums.OutputKindType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoMcpOutputConfig extends BoneIoOutputConfig {

    @JsonProperty("mcp_id")
    String mcpId;

    public BoneIoMcpOutputConfig() {
        super();
        kind = OutputKindType.MCP;
    }
}
