package com.dfi.sbc2ha.config.sbc2ha.definition.action;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionOutputType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OutputActionConfig extends ActionConfig {

    @JsonProperty("output")
    int output;

    @JsonProperty("action_output")
    ActionOutputType actionOutput;

    public OutputActionConfig() {
        super();
        action = ActionType.OUTPUT;
    }
}
