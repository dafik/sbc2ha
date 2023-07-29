package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
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

        this.payloadPress = Availability.getDefault(payloadPress, "reload");
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, getStateDeviceTypeName(), id, Availability.SET);

    }
}
