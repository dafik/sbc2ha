package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


public class CoverAvailability extends Availability {
    public static final String POS = "pos";
    @JsonProperty("position_topic")
    private final String positionTopic;
    @JsonProperty("device_class")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deviceClass;
    @JsonProperty("payload_open")
    private String payload_open = Constants.OPEN;
    @JsonProperty("payload_close")
    private String payloadClose = Constants.CLOSE;
    @JsonProperty("payload_stop")
    private String payloadStop = Constants.STOP;
    @JsonProperty("state_open")
    private String stateOpen = Constants.OPEN;
    @JsonProperty("state_opening")
    private String stateOpening = Constants.OPENING;
    @JsonProperty("state_closed")
    private String stateClosed = Constants.CLOSED;
    @JsonProperty("state_closing")
    private String state_closing = Constants.CLOSING;
    @JsonProperty("command_topic")
    private String commandTopic;
    @JsonProperty("set_position_topic")
    private String setPositionTopic;

    public CoverAvailability(String id, String name) {
        super(id, name, HaDeviceType.COVER, SbcDeviceType.COVER);
        String deviceTypeName = getStateDeviceTypeName();
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, deviceTypeName, id, Availability.SET);
        setPositionTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, deviceTypeName, id, POS);
        setStateTopic(Availability.formatTopic(Availability.topicPrefix, deviceTypeName, id, Constants.STATE));
        positionTopic = Availability.formatTopic(Availability.topicPrefix, deviceTypeName, id, POS);
    }

    public CoverAvailability(String id, String name, String deviceClass) {
        super(id, name, HaDeviceType.COVER, SbcDeviceType.COVER);
        String stateDeviceTypeName = getStateDeviceTypeName();
        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, Availability.SET);
        setPositionTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, POS);
        setStateTopic(Availability.formatTopic(Availability.topicPrefix, stateDeviceTypeName, id, Constants.STATE));
        positionTopic = Availability.formatTopic(Availability.topicPrefix, stateDeviceTypeName, id, POS);
        this.deviceClass = deviceClass;
    }
}
