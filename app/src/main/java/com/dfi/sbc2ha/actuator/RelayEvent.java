package com.dfi.sbc2ha.actuator;

import lombok.Data;

import static com.dfi.sbc2ha.actuator.ActuatorState.OFF;
import static com.dfi.sbc2ha.actuator.ActuatorState.ON;

@Data
public class RelayEvent implements ActuatorEvent<ActuatorState> {
    private ActuatorState state;
    private long eventTime;

    public static RelayEvent ofOn(long eventTime) {
        RelayEvent relayEvent = new RelayEvent();
        relayEvent.state = ON;
        relayEvent.eventTime = eventTime;
        return relayEvent;
    }

    public static RelayEvent ofOff(long eventTime) {
        RelayEvent relayEvent = new RelayEvent();
        relayEvent.state = OFF;
        relayEvent.eventTime = eventTime;
        return relayEvent;
    }

    public boolean toBoolean() {
        return state == ON;
    }

    public int toInt() {
        return state == ON ? 1 : 0;
    }
}
