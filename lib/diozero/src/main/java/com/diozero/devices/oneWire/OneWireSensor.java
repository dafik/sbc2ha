package com.diozero.devices.oneWire;

import com.diozero.devices.oneWire.bus.OneWireDevice;

public abstract class OneWireSensor {
    protected final OneWireDevice delegate;

    public OneWireSensor(OneWireDevice device) {
        delegate = device;
    }
}
