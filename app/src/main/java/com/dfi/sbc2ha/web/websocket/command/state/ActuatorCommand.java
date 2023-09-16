package com.dfi.sbc2ha.web.websocket.command.state;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class ActuatorCommand extends StateCommand {
    String actuator;

    public ActuatorCommand(String actuator) {
        super();
        this.actuator = actuator;
    }
}
