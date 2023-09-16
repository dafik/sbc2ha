package com.dfi.sbc2ha.web.websocket.command.state;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class SensorCommand extends StateCommand{
    String sensor;

    public SensorCommand(String sensor) {
        super();
        this.sensor = sensor;
    }
}
