package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.util.State;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface ActuatorEvent<S extends State> {

    @JsonProperty("state")
    S getState();

    void setState(ActuatorState state);

    long getEventTime();

}
