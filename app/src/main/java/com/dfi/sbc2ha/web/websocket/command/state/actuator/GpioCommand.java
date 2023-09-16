package com.dfi.sbc2ha.web.websocket.command.state.actuator;

import com.dfi.sbc2ha.services.state.actuator.ActuatorState;
import com.dfi.sbc2ha.web.websocket.command.state.ActuatorCommand;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonTypeName("gpio")
public class GpioCommand extends ActuatorCommand {
    String state;

    public GpioCommand(String actuator, String state) {
        super(actuator);
        this.state = state;
    }

    public ActuatorState getState() {
        ActuatorState actuatorState = ActuatorState.valueOf(state.toUpperCase());
        return actuatorState;
    }
}
