package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AdcSensorConfig {

    String id;
    String pin;
    @JsonProperty("update_interval")
    int updateInterval;
    Map<String, String> filters = new LinkedHashMap<>();
}
