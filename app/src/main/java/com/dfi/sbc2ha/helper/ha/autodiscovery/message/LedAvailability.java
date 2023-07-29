package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.EasingOld;
import com.dfi.sbc2ha.EasingType;
import com.dfi.sbc2ha.EasingVariant;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class LedAvailability extends Availability {
    public static final int DEFAULT_BRIGHTNESS_SCALE = 65535;
    public static final int LOCAL_BRIGHTNESS_SCALE = 1;


    @JsonProperty("command_topic")
    String commandTopic;
    @JsonProperty("brightness_scale")
    int brightnessScale = DEFAULT_BRIGHTNESS_SCALE;
    @JsonProperty("payload_off")
    String payloadOff = ActuatorState.OFF.toString().toLowerCase();
    @JsonProperty("payload_on")
    String payloadOn = ActuatorState.ON.toString().toLowerCase();
    @JsonProperty("state_value_template")
    String stateValueTemplate = "{{ value_json.state }}";
    @JsonProperty("brightness_value_template")
    String brightnessValueTemplate = "{{ value_json.brightness }}";
    String schema = "json";
    boolean brightness = true;
    @JsonProperty("color_mode")
    boolean colorMode = true;
    @JsonProperty("supported_color_modes")
    List<String> supportedColorModes = List.of("brightness");
    boolean effect = true;
    @JsonProperty("effect_list")
    List<String> effectList = EasingOld.names();

    public LedAvailability(String id, String name) {
        super(id, name, HaDeviceType.LIGHT, SbcDeviceType.RELAY);

        String stateDeviceTypeName = getStateDeviceTypeName();

        commandTopic = Availability.formatTopic(Availability.topicPrefix, Availability.CMD, stateDeviceTypeName, id, Availability.SET);


        if (System.getProperty("usePwmFadingLed") != null) {
            List<String> effectList = new ArrayList<>();
            Arrays.stream(EasingType.values()).forEach(easingType -> {
                Arrays.stream(EasingVariant.values()).forEach(variant -> {
                    effectList.add(easingType.name() + "-" + variant.name());
                });
            });
            this.effectList = effectList;
        }
    }

    public static int convertToHa(Float value) {
        return (int) (DEFAULT_BRIGHTNESS_SCALE * value / LOCAL_BRIGHTNESS_SCALE);
    }

    public static float convertFromHa(int value) {
        return (float) value * LOCAL_BRIGHTNESS_SCALE / DEFAULT_BRIGHTNESS_SCALE;

    }
}
