package com.dfi.sbc2ha.config.boneio.definition.action;


import com.dfi.sbc2ha.config.boneio.definition.enums.ActionOutputType;
import com.dfi.sbc2ha.config.boneio.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoOutputActionConfig extends BoneIoActionConfig {

    String pin;
    @JsonProperty("action_output")
    ActionOutputType actionOutput;

    public BoneIoOutputActionConfig() {
        super();
        action = ActionType.OUTPUT;
    }
}
