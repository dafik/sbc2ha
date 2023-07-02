package com.dfi.sbc2ha.sensor;

import com.diozero.api.Event;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = {"epochTime", "nanoTime"})
public abstract class StateEvent<S> extends Event {

    private S state;

    public StateEvent(long epochTime, long nanoTime, S state) {
        super(epochTime, nanoTime);
        this.state = state;
    }

    public StateEvent(long epochTime, long nanoTime) {
        super(epochTime, nanoTime);
    }

    public StateEvent() {
        super(System.currentTimeMillis(), System.nanoTime());
    }

    public StateEvent(S state) {
        super(System.currentTimeMillis(), System.nanoTime());
        this.state = state;
    }

    @JsonProperty("state")
    public S getState() {
        return state;
    }

    public void setState(S state) {
        this.state = state;
    }
}
