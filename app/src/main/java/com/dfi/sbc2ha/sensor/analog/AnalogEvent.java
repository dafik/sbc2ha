package com.dfi.sbc2ha.sensor.analog;

import com.dfi.sbc2ha.sensor.StateEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalogEvent extends StateEvent<AnalogState> {
    private float value;

    public AnalogEvent() {
        super();
    }

    public AnalogEvent(long epochTime, long nanoTime) {
        super(epochTime, nanoTime);
    }

    public AnalogEvent(long epochTime, long nanoTime, AnalogState state) {
        super(epochTime, nanoTime, state);
    }

    public AnalogEvent(long epochTime, long nanoTime, AnalogState state, float value) {
        super(epochTime, nanoTime, state);
        this.value = value;
    }

    public AnalogEvent(AnalogState state, float value) {
        super(state);
        this.value = value;
    }
}
