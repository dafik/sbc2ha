package com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum BoneIoCoverDeviceClassType implements BoneIoEnumLabel {
    /**
     * Generic cover. This is the default and does’t need to be set.
     */
    NONE("None"),
    /**
     * Control of an awning, such as an exterior retractable window, door, or patio cover.
     */
    AWNING("awning"),
    /**
     * Control of blinds, which are linked slats that expand or collapse to cover an opening or may be tilted to partially covering an opening, such as window blinds.
     */
    BLIND("blind"),
    /**
     * Control of curtains or drapes, which is often fabric hung above a window or door that can be drawn open.
     */
    CURTAIN("curtain"),
    /**
     * Control of a mechanical damper that reduces airflow, sound, or light.
     */
    DAMPER("damper"),
    /**
     * Control of a door or gate that provides access to an area.
     */
    DOOR("door"),
    /**
     * Control of a garage door that provides access to a garage.
     */
    GARAGE("garage"),
    /**
     * Control of a gate. Gates are found outside of a structure and are typically part of a fence.
     */
    GATE("gate"),
    /**
     * Control of shades, which are a continuous plane of material or connected cells that expanded or collapsed over an opening, such as window shades.
     */
    SHADE("shade"),
    /**
     * Control of shutters, which are linked slats that swing out/in to covering an opening or may be tilted to partially cover an opening, such as indoor or exterior window shutters.
     */
    SHUTTER("shutter"),
    /**
     * Control of a physical window that opens and closes or may tilt.
     */
    WINDOW("window");


    private final String label;


}
