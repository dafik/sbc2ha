package com.dfi.sbc2ha.config.boneio.definition;

import com.dfi.sbc2ha.config.boneio.definition.enums.LogLevelType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BoneIoLoggerConfig {

    @JsonProperty("default")
    LogLevelType defaultLevel;
    Map<String, LogLevelType> logs = new HashMap<>();
/*
logger:
  default: INFO
    logs:
        boneio: DEBUG
        boneio.mqtt_client: INFO
        boneio.sensor.temp: INFO
*/
}
