package com.dfi.sbc2ha.event.actuator;

import com.dfi.sbc2ha.EasingType;
import com.dfi.sbc2ha.EasingVariant;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.LedAvailability;
import com.dfi.sbc2ha.services.state.actuator.ActuatorState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties({/*"step", "easingType", "easingVariant",*/ "epochTime", "nanoTime"})
@JsonTypeName("ledFading")
public class LedFadingEvent extends StateEvent {

    float brightness;
    float step;
    EasingType easingType;
    EasingVariant easingVariant;


    public LedFadingEvent(ActuatorState state, float brightness, float step, EasingType easingType, EasingVariant easingVariant) {
        super(state.name());

        this.brightness = brightness;
        this.step = step;
        this.easingType = easingType;
        this.easingVariant = easingVariant;
    }

    public LedFadingEvent(ActuatorState state, float brightness, float step) {
        super(state.name());

        this.brightness = brightness;
        this.step = step;
    }

    public int getBrightness() {
        return LedAvailability.convertToHa(brightness);
    }

    public void setBrightness(float brightness) {
        this.brightness = LedAvailability.convertFromHa((int) brightness);
    }

    @JsonIgnore
    public float getBrightnessRaw() {
        return brightness;
    }


}
