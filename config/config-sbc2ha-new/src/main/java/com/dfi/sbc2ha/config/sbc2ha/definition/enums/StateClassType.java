package com.dfi.sbc2ha.config.sbc2ha.definition.enums;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.EnumLabel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StateClassType implements EnumLabel {
    MEASUREMENT("measurement"),
    TOTAL("total"),
    TOTAL_INCREASING("total_increasing");

    private final String label;
}
