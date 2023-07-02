package com.dfi.sbc2ha.config.sbc2ha.definition.output;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputKindType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

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
        @Type(value = GpioOutputConfig.class, name = "gpio")
})
@Data
public abstract class OutputConfig {

    String id;
    OutputKindType kind;
    @JsonProperty("output_type")
    OutputType outputType;
    String pin;
    @JsonProperty("restore_state")
    String restoreState;
    @JsonProperty("momentary_turn_on")
    String momentaryTurnOn;
    @JsonProperty("momentary_turn_off")
    String momentaryTurnOff;

    @JsonIgnore
    public String getName() {
        return Optional.of(id).orElse(pin);
    }



}
