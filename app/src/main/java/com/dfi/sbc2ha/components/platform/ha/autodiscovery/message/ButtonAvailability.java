package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


public class ButtonAvailability extends Availability {
    @Getter
    @JsonProperty("command_topic")
    String commandTopic;

    @JsonProperty("payload_press")
    String payloadPress;

    public ButtonAvailability(String id, String name, String payloadPress) {
        super(id, name, HaDeviceType.BUTTON, SbcDeviceType.BUTTON);

        this.payloadPress = getDefault(payloadPress, "reload");
        commandTopic = formatTopic(topicPrefix, CMD, getStateDeviceTypeName(), id, SET);

    }
}
