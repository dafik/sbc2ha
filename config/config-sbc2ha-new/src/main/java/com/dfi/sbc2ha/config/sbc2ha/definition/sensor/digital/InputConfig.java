package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital;

import com.dfi.sbc2ha.config.sbc2ha.definition.action.ActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ButtonState;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.InputKindType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "kind",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = InputSwitchConfig.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InputSensorConfig.class, name = "sensor"),
        @JsonSubTypes.Type(value = InputSwitchConfig.class, name = "switch"),
})
@Data
public abstract class InputConfig<A extends InputAction> extends SensorConfig {

    protected InputKindType kind;
    int input;

    @JsonProperty("click_detection")
    ButtonState clickDetection = ButtonState.LONG;

    @JsonProperty("bounce_time")
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    Duration bounceTime = DurationStyle.detectAndParse("25ms");

    @JsonProperty("show_in_ha")
    boolean showInHa = true;
    boolean inverted = false;

    Map<A, List<ActionConfig>> actions = new HashMap<>();

    public InputConfig() {
        super();
        platform = PlatformType.GPIO;
    }

    @JsonIgnore
    public List<ActionConfig> getEventActions(A event) {
        if (actions == null || !actions.containsKey(event)) {
            return List.of();
        }
        return actions.get(event);
    }
}
