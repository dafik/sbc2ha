package com.dfi.sbc2ha.event;

import com.dfi.sbc2ha.components.SbcDevice;
import com.dfi.sbc2ha.components.actuator.Actuator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class DeviceStateEvent {
    final StateEvent state;
    @JsonIgnore
    final private SbcDevice device;

    public DeviceStateEvent(StateEvent state, SbcDevice device) {
        this.state = state;
        this.device = device;
    }

    public String getName() {
        return device.getName();
    }

    public int getId() {
        return device instanceof Actuator ?  ((Actuator) device).getId() : -1;
    }

    public String getType() {
        return device instanceof Actuator ? "actuator" : "sensor";
    }
}
