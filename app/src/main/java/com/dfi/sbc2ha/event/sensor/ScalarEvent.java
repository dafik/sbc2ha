package com.dfi.sbc2ha.event.sensor;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.state.sensor.ScalarState;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("scalar")
public class ScalarEvent extends StateEvent {
    protected final float value;

    public ScalarEvent(long epochTime, long nanoTime, float value) {
        super(epochTime, nanoTime, ScalarState.CHANGED.name());
        this.value = value;
    }

    public ScalarEvent( float value) {
        super(ScalarState.CHANGED.name());
        this.value = value;
    }

    public float getValue() {
        return this.value;
    }

}
