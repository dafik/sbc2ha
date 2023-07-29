package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.StateClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ThermDeviceClassType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TempSensorAvailability extends Availability {

    @JsonProperty("device_class")
    String deviceClass = ThermDeviceClassType.TEMPERATURE.getSensorDeviceClassType().getLabel();
    @JsonProperty("state_class")
    String stateClass = StateClassType.MEASUREMENT.getLabel();
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement = "°C";

    public TempSensorAvailability(String id, String name) {
        super(id, name, HaDeviceType.SENSOR, SbcDeviceType.SENSOR);
        getDevice().setName("boneIO " + topicPrefix);
    }
}
