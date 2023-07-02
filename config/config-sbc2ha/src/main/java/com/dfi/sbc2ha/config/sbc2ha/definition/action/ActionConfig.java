package com.dfi.sbc2ha.config.sbc2ha.definition.action;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "action",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = OutputActionConfig.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CoverActionConfig.class, name = "cover"),
        @JsonSubTypes.Type(value = MqttActionConfig.class, name = "mqtt"),
        @JsonSubTypes.Type(value = OutputActionConfig.class, name = "output")
})
@Data
public abstract class ActionConfig {
    
    /*

      - action: output
        pin: 23 DZWONEK
        action_output: turn_on

     */

    protected ActionType action = ActionType.OUTPUT;

}
