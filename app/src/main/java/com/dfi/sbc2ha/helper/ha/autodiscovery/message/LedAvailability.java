package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.Easing;
import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class LedAvailability extends Availability {
    public static final int DEFAULT_BRIGHTNESS_SCALE = 65535;
    public static final int LOCAL_BRIGHTNES_SCALE = 1;


    @JsonProperty("command_topic")
    String commandTopic;
    //@JsonProperty("brightness_state_topic")
    //String brightnessStateTopic;
    //@JsonProperty("brightness_command_topic")
    //String brightnessCommandTopic;
    @JsonProperty("brightness_scale")
    int brightnessScale = DEFAULT_BRIGHTNESS_SCALE;
    @JsonProperty("payload_off")
    String payloadOff = Constants.OFF;
    @JsonProperty("payload_on")
    String payloadOn = Constants.ON;
    @JsonProperty("state_value_template")
    String stateValueTemplate = "{{ value_json.state }}";
    @JsonProperty("brightness_value_template")
    String brightnessValueTemplate = "{{ value_json.brightness }}";


    //name: mqtt_json_light_1
    //state_topic: "home/rgb1"
    //command_topic: "home/rgb1/set"

    @JsonProperty("schema")
    String schema = "json";

    @JsonProperty("brightness")
    boolean brightness = true;
    @JsonProperty("color_mode")
    boolean colorMode = true;
    @JsonProperty("supported_color_modes")
    List<String> supportedColorModes = List.of("brightness");

    boolean effect = true;
    @JsonProperty("effect_list")
    List<String> effectList = Easing.names();

    public LedAvailability(String id, String name) {
        super(id, name, HaDeviceType.LIGHT, SbcDeviceType.RELAY);

        String stateDeviceTypeName = getStateDeviceTypeName();

        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, Availability.SET);
        //brightnessStateTopic = Availability.formatTopic(Availability.topicPrefix, stateDeviceTypeName, id);
        //brightnessCommandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, Availability.SET_BRIGHTNESS);

    }

    public static int convertToHa(Float value) {
        return (int) (DEFAULT_BRIGHTNESS_SCALE * value / LOCAL_BRIGHTNES_SCALE);
    }

    public static float convertFromHa(int value) {
        return (float) value * LOCAL_BRIGHTNES_SCALE / DEFAULT_BRIGHTNESS_SCALE;

    }
}
