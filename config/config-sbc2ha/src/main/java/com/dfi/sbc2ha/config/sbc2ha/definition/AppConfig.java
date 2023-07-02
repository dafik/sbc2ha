package com.dfi.sbc2ha.config.sbc2ha.definition;

import com.dfi.sbc2ha.config.sbc2ha.definition.bus.BusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.bus.FsBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.bus.ModbusBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.extentsionBoard.ExtensionBoardsConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.output.OutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.AdcSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AppConfig {
    @JsonProperty("extension_boards")
    ExtensionBoardsConfig extensionBoards;

    MqttConfig mqtt;
    OledConfig oled;
    List<Lm75SensorConfig> lm75 = new ArrayList<>();
    List<BusConfig> mcp23017 = new ArrayList<>();
    List<BusConfig> ds2482 = new ArrayList<>();
    FsBusConfig dallas;
    ModbusBusConfig modbus;
    @JsonProperty("modbus_sensors")
    List<ModbusSensorConfig> modbusSensors = new ArrayList<>();

    List<SensorConfig> sensor = new ArrayList<>();

    List<OutputConfig> output = new ArrayList<>();
    List<InputConfig<?>> input = new ArrayList<>();

    List<AdcSensorConfig> adc = new ArrayList<>();
    LoggerConfig logger;


    @JsonIgnore
    public List<String> getOutputIds() {
        return getOutput().stream().map(OutputConfig::getId).collect(Collectors.toList());
    }
}
