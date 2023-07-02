package com.dfi.sbc2ha.config.sbc2ha.definition;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.extentsionBoard.ExtensionBoardsConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
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

    List<PlatformConfig> platform = new ArrayList<>();
    List<SensorConfig> sensor = new ArrayList<>();
    List<ActuatorConfig> actuator = new ArrayList<>();

    List<InputConfig<?>> input = new ArrayList<>();

    LoggerConfig logger;


    public void addSensors(List<SensorConfig> sensorList) {
        sensor.addAll(sensorList);
    }

    public List<PlatformConfig> getPlatform(PlatformType type) {
        return platform.stream()
                .filter(platformConfig -> platformConfig.getPlatform() == type)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getOutputIds() {
        return getActuator().stream().map(ActuatorConfig::getId).collect(Collectors.toList());
    }

    public void addPlatforms(List<PlatformConfig> platformConfigList) {
        platform.addAll(platformConfigList);
    }

    public void addPlatform(PlatformConfig platformConfig) {
        if (platformConfig == null) return;
        platform.add(platformConfig);
    }
}
