package com.dfi.sbc2ha.config.boneio.definition.output;


import com.dfi.sbc2ha.config.boneio.definition.enums.OutputKindType;
import com.dfi.sbc2ha.config.boneio.definition.enums.OutputType;
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
        defaultImpl = BoneIoMcpOutputConfig.class
)
@JsonSubTypes({
        @Type(value = BoneIoMcpOutputConfig.class, name = "mcp"),
        @Type(value = BoneIoPcaOutputConfig.class, name = "pca"),
        @Type(value = BoneIoGpioOutputConfig.class, name = "gpio")
})
@Data
public abstract class BoneIoOutputConfig {

    String id;
    OutputKindType kind;
    @JsonProperty("output_type")
    OutputType outputType;
    String pin;
    @JsonProperty("restore_state")
    boolean restoreState;
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
        return Optional.of(id).orElse(pin);
    }


}
