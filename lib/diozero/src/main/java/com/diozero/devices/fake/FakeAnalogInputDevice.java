package com.diozero.devices.fake;

import com.diozero.api.AnalogInputEvent;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.AbstractInputDevice;
import com.diozero.internal.spi.AnalogInputDeviceInterface;
import com.diozero.internal.spi.DeviceFactoryInterface;

import java.util.Random;

public class FakeAnalogInputDevice extends AbstractInputDevice<AnalogInputEvent> implements AnalogInputDeviceInterface {
    private final int deviceNumber;
    private Random rand;

    public FakeAnalogInputDevice(String s, DeviceFactoryInterface deviceFactoryInterface, int deviceNumber) {
        super(s, deviceFactoryInterface);
        this.deviceNumber = deviceNumber;
        rand = new Random();
    }

    @Override
    public int getAdcNumber() {
        return deviceNumber;
    }

    @Override
    public float getValue() throws RuntimeIOException {
        return rand.nextFloat();
    }
}
