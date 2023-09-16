package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


public class InputAvailability extends Availability {

    String icon = "mdi:gesture-double-tap";
    @JsonProperty("device_class")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String deviceClass;

    public InputAvailability(String id, String name) {
        super(id, name, HaDeviceType.SENSOR, SbcDeviceType.INPUT);
    }

    public InputAvailability(String id, String name, String deviceClass) {
        super(id, name, HaDeviceType.SENSOR, SbcDeviceType.INPUT);
        this.deviceClass = deviceClass;
    }

}
