package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class SwitchAvailability extends Availability {

    @JsonProperty("command_topic")
    String commandTopic;
    @JsonProperty("payload_off")
    String payloadOff = Constants.OFF;
    @JsonProperty("payload_on")
    String payloadOn = Constants.ON;
    @JsonProperty("value_template")
    String valueTemplate = "{{ value_json.state }}";


    public SwitchAvailability(String id, String name) {
        super(id, name, HaDeviceType.SWITCH, SbcDeviceType.RELAY);

        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, Constants.RELAY, id, Availability.SET);
    }

}
