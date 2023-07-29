package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TextFieldAvailability extends Availability {

    @JsonProperty("command_topic")
    String commandTopic;


    public TextFieldAvailability(String id, String name) {
        super(id, name, HaDeviceType.TEXT, SbcDeviceType.TEXT);
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, SbcDeviceType.TEXT.toLowerCase(), id, Availability.SET);
    }
}
