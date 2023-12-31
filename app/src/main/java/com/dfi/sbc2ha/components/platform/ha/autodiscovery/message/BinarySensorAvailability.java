package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.services.state.sensor.BinaryState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BinarySensorAvailability extends Availability {

    @JsonProperty("device_class")
    @JsonInclude(Include.NON_NULL)
    String deviceClass;
    @JsonProperty("payload_on")
    String payload_on = BinaryState.PRESSED.name().toLowerCase();
    @JsonProperty("payload_off")
    String payload_off = BinaryState.RELEASED.name().toLowerCase();

    public BinarySensorAvailability(String id, String name) {
        super(id, name, HaDeviceType.BINARY_SENSOR, SbcDeviceType.INPUTSENSOR);
        getDevice().setName("boneIO " + Availability.topicPrefix);
    }

    public BinarySensorAvailability(String id, String name, String deviceClass) {
        super(id, name, HaDeviceType.BINARY_SENSOR, SbcDeviceType.INPUTSENSOR);
        this.deviceClass = deviceClass;

        getDevice().setName("boneIO " + Availability.topicPrefix);
    }

    @Override
    public String getNodeName() {
        return getId().replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
