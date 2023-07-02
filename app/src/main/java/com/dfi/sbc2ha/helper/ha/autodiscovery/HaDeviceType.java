package com.dfi.sbc2ha.helper.ha.autodiscovery;

public enum HaDeviceType {
    ALARM_CONTROL_PANEL,
    BINARY_SENSOR,
    BUTTON,
    CAMERA,
    COVER,
    DEVICE_TRACKER,
    DEVICE_TRIGGER,
    DEVICE_AUTOMATION, //device trigger
    FAN,
    HUMIDIFIER,
    CLIMATE,
    LIGHT,
    LOCK,
    NUMBER,
    SCENE,
    SELECT,

    SENSOR,
    SIREN,
    SWITCH,
    UPDATE,
    TAG_SCANNER,
    TEXT,
    VACUUM;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
