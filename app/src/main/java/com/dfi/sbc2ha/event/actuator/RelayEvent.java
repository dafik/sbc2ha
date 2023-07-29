package com.dfi.sbc2ha.event.actuator;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.NoArgsConstructor;

import static com.dfi.sbc2ha.state.actuator.ActuatorState.ON;

@NoArgsConstructor
@JsonTypeName("relay")
public class RelayEvent extends StateEvent {
    public RelayEvent(long epochTime, long nanoTime, ActuatorState state) {
        super(epochTime, nanoTime, state.name());
    }

    public RelayEvent(ActuatorState state) {
        super(state.name());
    }

    public boolean toBoolean() {
        return state.equals(ON.name());
    }

    public int toInt() {
        return state.equals(ON.name()) ? 1 : 0;
    }
}
