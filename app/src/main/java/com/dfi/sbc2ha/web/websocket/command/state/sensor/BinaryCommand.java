package com.dfi.sbc2ha.web.websocket.command.state.sensor;

import com.dfi.sbc2ha.services.state.sensor.BinaryState;
import com.dfi.sbc2ha.web.websocket.command.state.SensorCommand;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonTypeName("binary")
public class BinaryCommand extends SensorCommand {
    String state;

    public BinaryCommand(String sensor, String state) {
        super(sensor);
        this.state = state;
    }

    public BinaryState getState() {
        BinaryState binaryState = BinaryState.valueOf(state.toUpperCase());
        return binaryState;
    }
}
