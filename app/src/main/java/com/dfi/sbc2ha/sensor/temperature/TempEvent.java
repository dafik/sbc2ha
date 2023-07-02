package com.dfi.sbc2ha.sensor.temperature;

import com.dfi.sbc2ha.sensor.StateEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempEvent extends StateEvent<TempState> {
    private float value;

    public TempEvent(long epochTime, long nanoTime, TempState state, float value) {
        super(epochTime, nanoTime, state);
        this.value = value;
    }
    public TempEvent(TempState state, float value) {
        super(state);
        this.value = value;
    }

    public TempEvent(long epochTime, long nanoTime, TempState state) {
        super(epochTime, nanoTime, state);
    }

    public TempEvent(long epochTime, long nanoTime) {
        super(epochTime, nanoTime);
    }

    public TempEvent() {
    }
}
