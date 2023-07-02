package com.dfi.sbc2ha.sensor.binary;

import com.dfi.sbc2ha.sensor.StateEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ButtonEvent extends StateEvent<ButtonState> {

    public ButtonEvent(ButtonState state) {
        super(state);
    }
}
