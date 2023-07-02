package com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SensorDeviceClassType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModbusDeviceClassType {
    ENERGY(SensorDeviceClassType.ENERGY),
    CURRENT(SensorDeviceClassType.CURRENT),
    VOLTAGE(SensorDeviceClassType.VOLTAGE);

    private final SensorDeviceClassType sensorDeviceClassType;
}
