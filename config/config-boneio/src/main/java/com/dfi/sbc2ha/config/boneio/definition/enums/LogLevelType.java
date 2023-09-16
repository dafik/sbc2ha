package com.dfi.sbc2ha.config.boneio.definition.enums;

public enum LogLevelType {
    ERROR("error"),
    WARN("warn"),
    INFO("info"),
    DEBUG("debug"),
    TRACE("trace");

    public final String level;

    LogLevelType(String level) {
        this.level = level;
    }
}
