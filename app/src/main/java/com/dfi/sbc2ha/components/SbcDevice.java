package com.dfi.sbc2ha.components;

import com.dfi.sbc2ha.event.DeviceStateEvent;
import com.dfi.sbc2ha.event.StateEvent;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

@Getter
public abstract class SbcDevice {

    protected final String name;
    private final EventBus eventBus = EventBus.getDefault();

    public SbcDevice(String name) {
        this.name = name;
    }

    protected void publishState(StateEvent event) {
        DeviceStateEvent deviceStateEvent = new DeviceStateEvent(event, this);
        eventBus.post(deviceStateEvent);
    }


}
