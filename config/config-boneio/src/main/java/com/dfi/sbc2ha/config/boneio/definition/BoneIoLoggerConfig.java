package com.dfi.sbc2ha.config.boneio.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.tinylog.Level;

import java.util.HashMap;
import java.util.Map;

@Data
public class BoneIoLoggerConfig {

    @JsonProperty("default")
    Level defaultLevel;
    Map<String, String> logs = new HashMap<>();
/*
logger:
  default: INFO
    logs:
        boneio: DEBUG
        boneio.mqtt_client: INFO
        boneio.sensor.temp: INFO
*/
}
