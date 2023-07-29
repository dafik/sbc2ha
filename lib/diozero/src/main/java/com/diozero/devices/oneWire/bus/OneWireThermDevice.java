package com.diozero.devices.oneWire.bus;

import com.diozero.api.RuntimeIOException;

public interface OneWireThermDevice extends OneWireDevice {
    float getTemperature() throws RuntimeIOException;
}
