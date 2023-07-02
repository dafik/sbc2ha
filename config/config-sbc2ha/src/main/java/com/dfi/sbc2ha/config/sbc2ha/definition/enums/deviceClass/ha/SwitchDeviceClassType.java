package com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SwitchDeviceClassType implements EnumLabel {
    /**
     * Generic switch. This is the default and does’t need to be set.
     */
    NONE("None"),
    /**
     * : This switch, switches a power outlet.
     */
    OUTLET("outlet"),
    /**
     * : A generic switch.
     */
    SWITCH("switch");

    private final String label;


}
