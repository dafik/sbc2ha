package com.dfi.sbc2ha.config.sbc2ha.definition.actuator;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputKindType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActuatorType;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.Duration;
import java.util.Optional;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "kind",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = McpOutputConfig.class
)
@JsonSubTypes({
        @Type(value = McpOutputConfig.class, name = "mcp"),
        @Type(value = PcaOutputConfig.class, name = "pca"),
        @Type(value = GpioOutputConfig.class, name = "gpio"),
        @Type(value = GpioPwmOutputConfig.class, name = "gpiopwm"),
        @Type(value = CoverConfig.class, name = "cover")
})
@Data
public abstract class ActuatorConfig {

    OutputKindType kind;

    @JsonProperty("name")
    String name;

    @JsonProperty("output_type")
    ActuatorType outputType;

    int output;

    /**
     * Send autodiscovery message to Home Assistant.
     * Optional
     */
    @JsonProperty("show_in_ha")
    boolean showInHa = true;

    /**
     * You can enable restore_state option. It’s bit experimental.
     * It saves state of relay or cover in state.json file, which is located in same directory as your config.json.
     * If output_type is None, then this value is overwritten to False!
     * Optional
     */
    @JsonProperty("restore_state")
    boolean restoreState = false;

    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("momentary_turn_on")
    Duration momentaryTurnOn;

    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("momentary_turn_off")
    Duration momentaryTurnOff;

    @JsonIgnore
    public String getName() {
        return Optional.of(name)
                .orElse(String.valueOf(output));
    }


}
