package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TextFieldAvailability extends Availability {

    @JsonProperty("command_topic")
    String commandTopic;


    public TextFieldAvailability(String id, String name) {
        super(id, name, HaDeviceType.TEXT, SbcDeviceType.TEXT);
        commandTopic = formatTopic(topicPrefix, CMD, SbcDeviceType.TEXT.toLowerCase(), id, SET);
    }
}
