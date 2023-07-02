package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class LightAvailability extends Availability {
    @JsonProperty("command_topic")
    String commandTopic;
    @JsonProperty("payload_off")
    String payloadOff = Constants.OFF;
    @JsonProperty("payload_on")
    String payloadOn = Constants.ON;
    @JsonProperty("state_value_template")
    String stateValueTemplate = "{{ value_json.state }}";

    public LightAvailability(String id, String name) {
        super(id, name, HaDeviceType.LIGHT, SbcDeviceType.RELAY);
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, getStateDeviceTypeName(), id, Availability.SET);
    }


}
