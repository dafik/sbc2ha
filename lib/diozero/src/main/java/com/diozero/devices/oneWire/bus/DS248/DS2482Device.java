package com.diozero.devices.oneWire.bus.DS248;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.OneWireSensor;
import com.diozero.devices.oneWire.bus.OneWireDevice;

public class DS2482Device implements OneWireDevice {

    protected OneWireSensor sensor;
    protected boolean initialized = false;
    protected byte[] state = null;

    public DS2482Device(OneWireSensor sensor) {
        this.sensor = sensor;
    }

    protected void initializeIfNeeded() throws OneWireException {
        if (!initialized) {
            initialize();
        }
    }

    public void initialize() throws OneWireException {

            state = sensor.readDevice();

    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public String getSerialNumber() {
        return null;
    }

    @Override
    public void close() {

    }
}
