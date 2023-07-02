package com.dfi.sbc2ha.config.boneio.definition.input;


import com.dfi.sbc2ha.config.boneio.definition.action.BoneIoActionConfig;
import com.dfi.sbc2ha.config.boneio.definition.enums.ButtonState;
import com.dfi.sbc2ha.config.boneio.definition.enums.InputKindType;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "kind",
        defaultImpl = BoneIoInputSwitchConfig.class

)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoneIoInputSensorConfig.class, name = "sensor"),
        @JsonSubTypes.Type(value = BoneIoInputSwitchConfig.class, name = "switch"),
})
@Data
public abstract class BoneIoInputConfig<A extends BoneIoInputAction> {
    
    /*
- id: IN_2_01 DZWONEK
  pin: P8_37
  gpio_mode: gpio_pu
  kind: switch
  actions:
    single:
      - action: output
        pin: 23 DZWONEK
        action_output: turn_on
    double:
      - action: output
        pin: 23 DZWONEK
        action_output: turn_on
    long:
      - action: output
        pin: 23 DZWONEK
        action_output: turn_on
     */


    protected InputKindType kind = InputKindType.SWITCH;
    String id;
    String pin;
    @JsonProperty("gpio_mode")
    String gpioMode;
    @JsonProperty("click_detection")
    ButtonState clickDetection = ButtonState.LONG;

    @JsonProperty("bounce_time")
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    Duration bounceTime = DurationStyle.detectAndParse("25ms");


    @JsonProperty("show_in_ha")
    boolean showInHa = true;
    boolean inverted = false;

    Map<A, List<BoneIoActionConfig>> actions = new HashMap<>();
//   actions (Optional, dictionary) - dictionary of predefined actions ([single, double, long], [pressed, released]).


    @JsonIgnore
    public List<BoneIoActionConfig> getEventActions(A event) {
        if (actions == null || !actions.containsKey(event)) {
            return List.of();
        }
        return actions.get(event);
    }
}
