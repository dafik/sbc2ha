package com.dfi.sbc2ha.config.sbc2ha.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.tinylog.Level;

import java.util.HashMap;
import java.util.Map;

@Data
public class LoggerConfig {

    @JsonProperty("default")
    Level defaultLevel;
    Map<String, String> logs = new HashMap<>();
    Map<String, String> writer = new HashMap<>();
    //writerConsole.format = {date:HH:mm:ss.SSS} [{thread|size=30}] {level|size=5} {class|size=50}::{method|size=35} - {message}

/*
logger:
  default: INFO
    logs:
        boneio: DEBUG
        boneio.mqtt_client: INFO
        boneio.sensor.temp: INFO
*/
}
