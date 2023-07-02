package com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoneIoButtonDeviceClassType implements BoneIoEnumLabel {
    /**
     * Generic switch. This is the default and does’t need to be set.
     */
    NONE("None"),
    /**
     * : This switch, switches a power outlet.
     */
    RESTART("restart"),
    /**
     * : A generic switch.
     */
    UPDATE("update");

    private final String label;


}
