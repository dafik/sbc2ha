package com.dfi.sbc2ha.config.boneio.definition.sensor;



import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;

@Data
public class BoneIoModbusSensorConfig {

    String id;
    int address;
    String model;
    @JsonProperty("update_interval")
    Duration updateInterval = DurationStyle.detectAndParse("60s");
}
