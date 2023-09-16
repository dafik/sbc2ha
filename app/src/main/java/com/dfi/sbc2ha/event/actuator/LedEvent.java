package com.dfi.sbc2ha.event.actuator;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.LedAvailability;
import com.dfi.sbc2ha.services.state.actuator.ActuatorState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonTypeName("led")
public class LedEvent extends StateEvent {

    private float brightness;

    public LedEvent(long epochTime, long nanoTime, ActuatorState state) {
        super(epochTime, nanoTime, state.name());
    }

    public LedEvent(ActuatorState state) {
        super(state.name());
    }

    public LedEvent(ActuatorState state, float brightness) {
        super(state.name());

        this.brightness = brightness;
    }

    public int getBrightness() {
        return LedAvailability.convertToHa(brightness);
    }

    @JsonIgnore
    public float getBrightnessRaw() {
        return brightness;
    }


}
