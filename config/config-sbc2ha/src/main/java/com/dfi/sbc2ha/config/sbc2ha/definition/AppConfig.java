package com.dfi.sbc2ha.config.sbc2ha.definition;

import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.extentsionBoard.ExtensionBoardsConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.PlatformConfig;
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

    List<PlatformConfig> platform = new ArrayList<>();
    List<SensorConfig> sensor = new ArrayList<>();
    List<ActuatorConfig> actuator = new ArrayList<>();
    LoggerConfig logger;


    public void addSensors(List<SensorConfig> sensorList) {
        sensor.addAll(sensorList);
    }

    public List<SensorConfig> getSensor(PlatformType type) {
        return sensor.stream()
                .filter(sensorConfig -> sensorConfig.getPlatform() == type)
                .collect(Collectors.toList());
    }

    public <T extends SensorConfig> List<T> getSensor(PlatformType type, Class<T> sensorClass) {
        return sensor.stream()
                .filter(sensorConfig -> sensorConfig.getPlatform() == type)
                .map(sensorClass::cast)
                .collect(Collectors.toList());
    }

    public List<PlatformConfig> getPlatform(PlatformType type) {
        return platform.stream()
                .filter(platformConfig -> platformConfig.getPlatform() == type)
                .collect(Collectors.toList());
    }

    public <T extends PlatformConfig> List<T> getPlatform(PlatformType type, Class<T> listClass) {
        return platform.stream()
                .filter(platformConfig -> platformConfig.getPlatform() == type)
                .map(listClass::cast)
                .collect(Collectors.toList());
    }

    public MqttConfig mqttConfig(){
        List<MqttConfig> mqttConfigList = getPlatform(PlatformType.MQTT, MqttConfig.class);
        if(mqttConfigList.size() == 0){
            return null;
        }
        return mqttConfigList.get(0);

    }

    @JsonIgnore
    public List<String> getActuatorLabels() {
        return getActuator().stream().map(ActuatorConfig::getName).collect(Collectors.toList());
    }

    public void addPlatforms(List<PlatformConfig> platformConfigList) {
        platform.addAll(platformConfigList);
    }

    public void addPlatform(PlatformConfig platformConfig) {
        if (platformConfig == null) return;
        platform.add(platformConfig);
    }

    public void addActuator(List<ActuatorConfig> coverList) {
        actuator.addAll(coverList);
    }
}
