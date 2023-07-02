package com.dfi.sbc2ha.helper.ha;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.EnumLabel;
import com.dfi.sbc2ha.util.State;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceState implements EnumLabel, State {
    ONLINE("online"),
    OFFLINE("offline");

    private final String label;
}
