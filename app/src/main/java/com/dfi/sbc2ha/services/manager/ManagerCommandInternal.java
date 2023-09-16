package com.dfi.sbc2ha.services.manager;

import com.dfi.sbc2ha.config.sbc2ha.definition.action.ActionConfig;
import lombok.Data;

import java.util.List;

@Data
public class ManagerCommandInternal implements ManagerCommand {
    private final List<ActionConfig> eventActions;

    public ManagerCommandInternal(List<ActionConfig> eventActions) {

        this.eventActions = eventActions;
    }
}
