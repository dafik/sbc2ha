package com.dfi.sbc2ha.web.websocket.command.state.sensor;

import com.dfi.sbc2ha.services.state.sensor.ButtonState;
import com.dfi.sbc2ha.web.websocket.command.state.SensorCommand;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonTypeName("button")
public class ButtonCommand extends SensorCommand {
    String state;

    public ButtonCommand(String sensor, String state) {
        super(sensor);
        this.state = state;
    }

    public ButtonState getState() {
        ButtonState buttonState = ButtonState.valueOf(state.toUpperCase());
        return buttonState;
    }
}
