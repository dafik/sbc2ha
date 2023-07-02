package com.dfi.sbc2ha.config.boneio.definition.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum OutputKindType {
    GPIO,
    MCP,
    PCA;

    @JsonIgnore
    public String toLowerCase() {
        return name().toLowerCase();
    }
}
