package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ValueSensorConfig extends SensorConfig {

    @JsonProperty("update_interval")
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    Duration updateInterval = DurationStyle.detectAndParse("60s");

    List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

    public ValueSensorConfig() {
        super();
    }
}
