package com.dfi.sbc2ha.config.boneio.definition.action;


import com.dfi.sbc2ha.config.boneio.definition.enums.ActionCoverType;
import com.dfi.sbc2ha.config.boneio.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoCoverActionConfig extends BoneIoActionConfig {

    String pin;
    @JsonProperty("action_cover")
    ActionCoverType actionCover;

    public BoneIoCoverActionConfig() {
        super();
        action = ActionType.COVER;
    }
}
