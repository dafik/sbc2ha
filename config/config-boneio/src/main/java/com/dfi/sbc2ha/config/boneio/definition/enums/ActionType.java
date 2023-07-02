package com.dfi.sbc2ha.config.boneio.definition.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum ActionType {
    MQTT,
    OUTPUT,
    COVER;

    @JsonIgnore
    public String toLowerCase() {
        return name().toLowerCase();
    }
}
