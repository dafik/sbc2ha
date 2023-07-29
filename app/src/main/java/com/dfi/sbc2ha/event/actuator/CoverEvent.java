package com.dfi.sbc2ha.event.actuator;

import com.dfi.sbc2ha.actuator.Cover;
import com.dfi.sbc2ha.event.StateEvent;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonTypeName("cover")
public class CoverEvent extends StateEvent {

    @Getter
    private float position;

    public CoverEvent(long epochTime, long nanoTime, Cover.CoverState state, float position) {
        super(epochTime, nanoTime, state.name());
        this.position = position;
    }

    public CoverEvent(Cover.CoverState state, float position) {
        super(state.name());
        this.position = position;
    }


}
