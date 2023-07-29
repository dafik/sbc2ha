package com.dfi.sbc2ha.config.boneio.definition;

import com.dfi.sbc2ha.config.boneio.definition.bus.BoneIoBusConfig;
import com.dfi.sbc2ha.config.boneio.definition.bus.BoneIoFsBusConfig;
import com.dfi.sbc2ha.config.boneio.definition.bus.BoneIoModbusBusConfig;
import com.dfi.sbc2ha.config.boneio.definition.cover.BoneioCoverConfig;
import com.dfi.sbc2ha.config.boneio.definition.input.BoneIoInputConfig;
import com.dfi.sbc2ha.config.boneio.definition.output.BoneIoOutputConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoAdcSensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoLm75SensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoModbusSensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoSensorConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BoneIoConfig {

    BoneIoBMqttConfig mqtt;
    BoneIoOledConfig oled;
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoLm75SensorConfig> lm75 = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoBusConfig> mcp23017 = new ArrayList<>();
    List<BoneIoBusConfig> pca9685 = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoBusConfig> ds2482 = new ArrayList<>();
    BoneIoFsBusConfig dallas;
    BoneIoModbusBusConfig modbus;
    @JsonProperty("modbus_sensors")
    List<BoneIoModbusSensorConfig> modbusSensors = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoSensorConfig> sensor = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneioCoverConfig> cover = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoOutputConfig> output = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoInputConfig<?>> input = new ArrayList<>();
    @JsonSetter(nulls = Nulls.SKIP)
    List<BoneIoAdcSensorConfig> adc = new ArrayList<>();
    BoneIoLoggerConfig logger;
}


