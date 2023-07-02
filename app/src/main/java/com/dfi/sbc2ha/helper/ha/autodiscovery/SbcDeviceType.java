package com.dfi.sbc2ha.helper.ha.autodiscovery;

public enum SbcDeviceType {
    SENSOR,
    INPUTSENSOR,
    INPUT,
    RELAY,
    BUTTON,
    COVER;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
