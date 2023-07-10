package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.deserializer.InputConfigDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = InputConfigDeserializer.class)
public class FakeInputConfig extends SensorConfig {
}
