package com.dfi.sbc2ha.config.sbc2ha.definition.action;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionCoverType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CoverActionConfig extends ActionConfig {

    String pin;
    @JsonProperty("action_cover")
    ActionCoverType actionCover;

    public CoverActionConfig() {
        super();
        action = ActionType.COVER;
    }
}
