package com.diozero.devices.oneWire;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.ThermometerInterface;
import com.diozero.devices.oneWire.bus.OneWireDevice.Type;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;


public class OneWireThermSensor extends OneWireSensor implements ThermometerInterface {

    public OneWireThermSensor(OneWireThermDevice device) {
        super(device);

    }

    /**
     * Get temperature in degrees celsius
     *
     * @return Temperature (deg C)
     * @throws RuntimeIOException if an I/O error occurs
     */
    @Override
    public float getTemperature() throws RuntimeIOException {

        return ((OneWireThermDevice) delegate).getTemperature();
    }

    public Type getType() {
        return delegate.getType();
    }

    public String getSerialNumber() {
        return delegate.getSerialNumber();
    }

    @Override
    public void close() {
        // nop
        delegate.close();
    }
}
