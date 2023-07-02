package com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass;


import com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha.BoneIoSensorDeviceClassType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoneIoModbusDeviceClassType {
    ENERGY(BoneIoSensorDeviceClassType.ENERGY),
    CURRENT(BoneIoSensorDeviceClassType.CURRENT),
    VOLTAGE(BoneIoSensorDeviceClassType.VOLTAGE);

    private final BoneIoSensorDeviceClassType sensorDeviceClassType;
}
