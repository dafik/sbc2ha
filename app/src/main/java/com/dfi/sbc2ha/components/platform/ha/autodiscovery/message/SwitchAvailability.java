package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.services.state.actuator.ActuatorState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


public class SwitchAvailability extends Availability {

    @Getter
    @JsonProperty("command_topic")
    String commandTopic;
    @JsonProperty("payload_off")
    String payloadOff = ActuatorState.OFF.toString().toLowerCase();
    @JsonProperty("payload_on")
    String payloadOn = ActuatorState.ON.toString().toLowerCase();
    //@JsonProperty("value_template")
   // String valueTemplate = "{{ value_json.state }}";


    public SwitchAvailability(String id, String name) {
        super(id, name, HaDeviceType.SWITCH, SbcDeviceType.RELAY);

        commandTopic = formatTopic(topicPrefix, CMD, SbcDeviceType.RELAY.toLowerCase(), id, SET);
    }

}
