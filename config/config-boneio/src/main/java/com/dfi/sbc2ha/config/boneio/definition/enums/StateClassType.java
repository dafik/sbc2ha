package com.dfi.sbc2ha.config.boneio.definition.enums;


import com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha.BoneIoEnumLabel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StateClassType implements BoneIoEnumLabel {
    MEASUREMENT("measurement"),
    TOTAL("total"),
    TOTAL_INCREASING("total_increasing");

    private final String label;
}
