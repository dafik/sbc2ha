package com.diozero.devices.oneWire.bus;

import com.diozero.api.RuntimeIOException;

public interface OneWireThermDevice extends OneWireDevice {
    public float getTemperature() throws RuntimeIOException;
}
