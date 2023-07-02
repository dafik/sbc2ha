package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SensorConfig {
    protected PlatformType platform;
    String id;
    @JsonProperty("show_in_ha")
    boolean showInHa = true;
}
