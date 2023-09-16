package com.dfi.sbc2ha.components.platform.ha.autodiscovery;

public enum SbcDeviceType {
    SENSOR,
    INPUTSENSOR,
    INPUT,
    RELAY,
    BUTTON,
    COVER,
    TEXT;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
