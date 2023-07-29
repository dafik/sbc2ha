package com.dfi.sbc2ha.event.sensor;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.state.sensor.ButtonState;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("button")
public class ButtonEvent extends StateEvent {

    public ButtonEvent(long epochTime, long nanoTime, ButtonState state) {
        super(epochTime, nanoTime, state.name());
    }

    public ButtonEvent(ButtonState state) {
        super(state.name());

    }
}
