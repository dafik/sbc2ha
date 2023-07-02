package com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass;


import com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha.BoneIoSensorDeviceClassType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoneIoThermDeviceClassType {
    TEMPERATURE(BoneIoSensorDeviceClassType.TEMPERATURE);

    private final BoneIoSensorDeviceClassType sensorDeviceClassType;

}
