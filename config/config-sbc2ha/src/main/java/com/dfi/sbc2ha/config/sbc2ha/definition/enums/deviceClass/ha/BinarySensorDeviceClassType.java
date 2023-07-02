package com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BinarySensorDeviceClassType implements EnumLabel {
    /**
     * Generic on/off. This is the default and does’t need to be set.
     */
    NONE("None"),
    /**
     * on means low, off means normal
     */
    BATTERY("battery"),
    /**
     * on means charging, off means not charging
     */
    BATTERY_CHARGING("battery_charging"),
    /**
     * on means carbon monoxide detected, off no carbon monoxide (clear)
     */
    CARBON_MONOXIDE("carbon_monoxide"),
    /**
     * on means cold, off means normal
     */
    COLD("cold"),
    /**
     * on means connected, off means disconnected
     */
    CONNECTIVITY("connectivity"),
    /**
     * on means open, off means closed
     */
    DOOR("door"),
    /**
     * on means open, off means closed
     */
    GARAGE_DOOR("garage_door"),
    /**
     * on means gas detected, off means no gas (clear)
     */
    GAS("gas"),
    /**
     * on means hot, off means normal
     */
    HEAT("heat"),
    /**
     * on means light detected, off means no light
     */
    LIGHT("light"),
    /**
     * on means open (unlocked), off means closed (locked)
     */
    LOCK("lock"),
    /**
     * on means moisture detected (wet), off means no moisture (dry)
     */
    MOISTURE("moisture"),
    /**
     * on means motion detected, off means no motion (clear)
     */
    MOTION("motion"),
    /**
     * on means moving, off means not moving (stopped)
     */
    MOVING("moving"),
    /**
     * on means occupied (detected), off means not occupied (clear)
     */
    OCCUPANCY("occupancy"),
    /**
     * on means open, off means closed
     */
    OPENING("opening"),
    /**
     * on means device is plugged in, off means device is unplugged
     */
    PLUG("plug"),
    /**
     * on means power detected, off means no power
     */
    POWER("power"),
    /**
     * on means home, off means away
     */
    PRESENCE("presence"),
    /**
     * on means problem detected, off means no problem (OK)
     */
    PROBLEM("problem"),
    /**
     * on means running, off means not running
     */
    RUNNING("running"),
    /**
     * on means unsafe, off means safe
     */
    SAFETY("safety"),
    /**
     * on means smoke detected, off means no smoke (clear)
     */
    SMOKE("smoke"),
    /**
     * on means sound detected, off means no sound (clear)
     */
    SOUND("sound"),
    /**
     * on means tampering detected, off means no tampering (clear)
     */
    TAMPER("tamper"),
    /**
     * on means update available, off means up-to-date
     */
    UPDATE("update"),
    /**
     * on means vibration detected, off means no vibration (clear)
     */
    VIBRATION("vibration"),
    /**
     * on means open, off means closed
     */
    WINDOW("window");

    private final String label;
}
