package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


public class LightAvailability extends Availability {

    @Getter
    @JsonProperty("command_topic")
    String commandTopic;
    @JsonProperty("payload_off")
    String payloadOff = ActuatorState.OFF.toString().toLowerCase();
    @JsonProperty("payload_on")
    String payloadOn = ActuatorState.ON.toString().toLowerCase();
    //@JsonProperty("state_value_template")
    //String stateValueTemplate = "{{ value_json.state }}";

    public LightAvailability(String id, String name) {
        super(id, name, HaDeviceType.LIGHT, SbcDeviceType.RELAY);
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, getStateDeviceTypeName(), id, Availability.SET);
    }


}
