package com.diozero.devices.fake;

import com.diozero.api.*;
import com.diozero.internal.spi.*;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.LocalSystemInfo;

public class FakeDeviceFactory extends BaseNativeDeviceFactory  {

    @Override
    protected BoardInfo lookupBoardInfo() {
        return new FakeBoardInfo(LocalSystemInfo.getInstance());
    }

    @Override
    public AnalogInputDeviceInterface createAnalogInputDevice(String s, PinInfo pinInfo) {
        return new FakeAnalogInputDevice(s, this, pinInfo.getDeviceNumber());
    }

    @Override
    public GpioDigitalInputDeviceInterface createDigitalInputDevice(String s, PinInfo pinInfo, GpioPullUpDown gpioPullUpDown, GpioEventTrigger gpioEventTrigger) {
        return new FakeGpioDigitalInputDevice(s, this, pinInfo.getDeviceNumber());
    }

    @Override
    public GpioDigitalOutputDeviceInterface createDigitalOutputDevice(String s, PinInfo pinInfo, boolean b) {
        return new FakeGpioDigitalOutputDevice(s, this, pinInfo.getDeviceNumber(),b);
    }

    @Override
    public GpioDigitalInputOutputDeviceInterface createDigitalInputOutputDevice(String s, PinInfo pinInfo, DeviceMode deviceMode) {
        return new FakeGpioDigitalInputOutputDevice(s, this, pinInfo.getDeviceNumber());
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getGpioValue(int gpio) {
        return 0;
    }

    @Override
    public DeviceMode getGpioMode(int gpio) {
        return null;
    }

    @Override
    public AnalogOutputDeviceInterface createAnalogOutputDevice(String key, PinInfo pinInfo, float initialValue) {
        return null;
    }

    @Override
    public InternalI2CDeviceInterface createI2CDevice(String key, int controller, int address, I2CConstants.AddressSize addressSize) throws RuntimeIOException {
        return null;
    }

    @Override
    public int getBoardPwmFrequency() {
        return 0;
    }

    @Override
    public void setBoardPwmFrequency(int pwmFrequency) {

    }

    @Override
    public InternalPwmOutputDeviceInterface createPwmOutputDevice(String key, PinInfo pinInfo, int pwmFrequency, float initialValue) {
        return null;
    }

    @Override
    public InternalSerialDeviceInterface createSerialDevice(String key, String deviceFilename, int baud, SerialConstants.DataBits dataBits, SerialConstants.StopBits stopBits, SerialConstants.Parity parity, boolean readBlocking, int minReadChars, int readTimeoutMillis) throws RuntimeIOException {
        return null;
    }

    @Override
    public int getBoardServoFrequency() {
        return 0;
    }

    @Override
    public void setBoardServoFrequency(int servoFrequency) {

    }

    @Override
    public InternalServoDeviceInterface createServoDevice(String key, PinInfo pinInfo, int frequencyHz, int minPulseWidthUs, int maxPulseWidthUs, int initialPulseWidthUs) {
        return null;
    }

    @Override
    public InternalSpiDeviceInterface createSpiDevice(String key, int controller, int chipSelect, int frequency, SpiClockMode spiClockMode, boolean lsbFirst) throws RuntimeIOException {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
