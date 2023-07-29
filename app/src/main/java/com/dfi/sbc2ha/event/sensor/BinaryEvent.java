package com.dfi.sbc2ha.event.sensor;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.state.sensor.BinaryState;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("binary")
public class BinaryEvent extends StateEvent {

    public BinaryEvent(long epochTime, long nanoTime, BinaryState state) {
        super(epochTime, nanoTime, state.name());
    }

    public BinaryEvent(BinaryState state) {
        super(state.name());

    }
}
