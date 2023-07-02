package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.helper.ha.autodiscovery.message.LedAvailability;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedEvent implements ActuatorEvent<ActuatorState> {

    @JsonIgnore
    long eventTime;
    private float brightness;
    private ActuatorState state;


    public int getBrightness() {
        return LedAvailability.convertToHa(brightness);
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    @JsonIgnore
    public float getBrightnessRaw() {
        return brightness;
    }


}
