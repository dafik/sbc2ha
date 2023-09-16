package com.diozero.devices.fake;

import com.diozero.api.DeviceMode;
import com.diozero.api.DigitalInputEvent;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.AbstractInputDevice;
import com.diozero.internal.spi.DeviceFactoryInterface;
import com.diozero.internal.spi.GpioDigitalInputOutputDeviceInterface;

public class FakeGpioDigitalInputOutputDevice extends AbstractInputDevice<DigitalInputEvent> implements GpioDigitalInputOutputDeviceInterface {
    private final int deviceNumber;
    private boolean value = false;

    public FakeGpioDigitalInputOutputDevice(String s, DeviceFactoryInterface deviceFactoryInterface, int deviceNumber) {
        super(s, deviceFactoryInterface);
        this.deviceNumber = deviceNumber;
    }

    @Override
    public void setMode(DeviceMode deviceMode) {

    }

    @Override
    public boolean getValue() throws RuntimeIOException {
        return value;
    }

    @Override
    public void setValue(boolean b) throws RuntimeIOException {
        value = b;
    }

    @Override
    public int getGpio() {
        return 0;
    }
}
