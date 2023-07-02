package com.dfi.sbc2ha.config.boneio.definition.sensor;

import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import lombok.Data;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class BoneIoLm75SensorConfig {

    String id;
    int address;
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement = "°C";
    @JsonProperty("update_interval")
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    Duration updateInterval = DurationStyle.detectAndParse("60s");
    Map<String, String> filters = new LinkedHashMap<>();
}
