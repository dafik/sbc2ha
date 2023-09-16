package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.components.actuator.Cover;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


public class CoverAvailability extends Availability {
    public static final String POS = "pos";
    @Getter
    @JsonProperty("position_topic")
    private final String positionTopic;
    @JsonProperty("device_class")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deviceClass;
    @JsonProperty("payload_open")
    private String payload_open = Cover.CoverAction.OPEN.toString().toLowerCase();
    @JsonProperty("payload_close")
    private String payloadClose = Cover.CoverAction.CLOSE.toString().toLowerCase();
    @JsonProperty("payload_stop")
    private String payloadStop = Cover.CoverAction.STOP.toString().toLowerCase();
    @JsonProperty("state_open")
    private String stateOpen = Cover.CoverState.OPEN.toString().toLowerCase();
    @JsonProperty("state_opening")
    private String stateOpening = Cover.CoverState.OPENING.toString().toLowerCase();
    @JsonProperty("state_closed")
    private String stateClosed = Cover.CoverState.CLOSED.toString().toLowerCase();
    @JsonProperty("state_closing")
    private String state_closing = Cover.CoverState.CLOSING.toString().toLowerCase();
    @Getter
    @JsonProperty("command_topic")
    private String commandTopic;
    @Getter
    @JsonProperty("set_position_topic")
    private String setPositionTopic;

    public CoverAvailability(String id, String name) {
        super(id, name, HaDeviceType.COVER, SbcDeviceType.COVER);
        String deviceTypeName = getStateDeviceTypeName();
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, deviceTypeName, id, Availability.SET);
        setPositionTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, deviceTypeName, id, POS);
        setStateTopic(Availability.formatTopic(Availability.topicPrefix, deviceTypeName, id, STATE));
        positionTopic = Availability.formatTopic(Availability.topicPrefix, deviceTypeName, id, POS);
    }

    public CoverAvailability(String id, String name, String deviceClass) {
        super(id, name, HaDeviceType.COVER, SbcDeviceType.COVER);
        String stateDeviceTypeName = getStateDeviceTypeName();
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, Availability.SET);
        setPositionTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, POS);
        setStateTopic(Availability.formatTopic(Availability.topicPrefix, stateDeviceTypeName, id, STATE));
        positionTopic = Availability.formatTopic(Availability.topicPrefix, stateDeviceTypeName, id, POS);
        this.deviceClass = deviceClass;
    }
}
