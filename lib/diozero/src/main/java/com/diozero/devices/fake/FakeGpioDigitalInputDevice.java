package com.diozero.devices.fake;

import com.diozero.api.DigitalInputEvent;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.AbstractInputDevice;
import com.diozero.internal.spi.DeviceFactoryInterface;
import com.diozero.internal.spi.GpioDigitalInputDeviceInterface;

public class FakeGpioDigitalInputDevice extends AbstractInputDevice<DigitalInputEvent> implements GpioDigitalInputDeviceInterface {
    public FakeGpioDigitalInputDevice(String s, DeviceFactoryInterface deviceFactoryInterface, int deviceNumber) {
        super(s, deviceFactoryInterface);
    }

    @Override
    public void setDebounceTimeMillis(int i) {

    }

    @Override
    public boolean getValue() throws RuntimeIOException {
        return false;
    }

    @Override
    public int getGpio() {
        return 0;
    }
}
