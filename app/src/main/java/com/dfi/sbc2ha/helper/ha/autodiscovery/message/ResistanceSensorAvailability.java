package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.StateClassType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ResistanceSensorAvailability extends Availability {

    @JsonProperty("unit_of_measurement")
    private final String unitOfMeasurement = "Ω";
    @JsonProperty("state_class")
    private final String stateClass = StateClassType.MEASUREMENT.getLabel();
    @JsonProperty("device_class")
    private String deviceClass;


    public ResistanceSensorAvailability(String id, String name) {
        super(id, name, HaDeviceType.SENSOR, SbcDeviceType.SENSOR);
    }
}
