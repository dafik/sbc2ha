package com.dfi.sbc2ha.sensor.binary;

import com.dfi.sbc2ha.sensor.StateEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaryEvent extends StateEvent<BinaryState> {

    public BinaryEvent(BinaryState state) {
        super(state);
    }
}
