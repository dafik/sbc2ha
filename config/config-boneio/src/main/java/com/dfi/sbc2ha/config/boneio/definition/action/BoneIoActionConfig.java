package com.dfi.sbc2ha.config.boneio.definition.action;


import com.dfi.sbc2ha.config.boneio.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "action",
        defaultImpl = BoneIoOutputActionConfig.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoneIoCoverActionConfig.class, name = "cover"),
        @JsonSubTypes.Type(value = BoneIoMqttActionConfig.class, name = "mqtt"),
        @JsonSubTypes.Type(value = BoneIoOutputActionConfig.class, name = "output")
})
@Data
public abstract class BoneIoActionConfig {
    
    /*

      - action: output
        pin: 23 DZWONEK
        action_output: turn_on

     */

    protected ActionType action = ActionType.OUTPUT;

}
