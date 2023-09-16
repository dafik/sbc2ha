package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.StateClassType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class AnalogSensorAvailability extends Availability {

    @JsonProperty("unit_of_measurement")
    private final String unitOfMeasurement = "V";
    @JsonProperty("device_class")
    private final String deviceClass = "voltage";
    @JsonProperty("state_class")
    private final String stateClass = StateClassType.MEASUREMENT.getLabel();


    public AnalogSensorAvailability(String id, String name) {
        super(id, name, HaDeviceType.SENSOR, SbcDeviceType.SENSOR);
    }
}
