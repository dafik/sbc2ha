package com.diozero.devices.oneWire.bus.fs;

import com.diozero.devices.oneWire.bus.OneWireDevice;

/**
 * @see <a href="https://www.maximintegrated.com/en/products/analog/sensors-and-sensor-interface/DS18B20.html">DS18B20 Programmable Resolution 1-Wire Digital Thermometer</a>
 * @see <a href="https://learn.adafruit.com/adafruits-raspberry-pi-lesson-11-ds18b20-temperature-sensing?view=all">Adafruit's Raspberry Pi Lesson 11. DS18B20 Temperature Sensing</a>
 * @see <a href="https://github.com/timofurrer/w1thermsensor">W1ThermSensor Python library</a>
 */
public class FsDevice implements OneWireDevice {
    protected final Type type;
    protected final String serialNumber;
    protected final FsBus oneWireBus;

    public FsDevice(FsBus bus, Type type, String serialNumber) {
        this.oneWireBus = bus;
        this.type = type;
        this.serialNumber = serialNumber;

        bus.checkDeviceExist(type, serialNumber);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }


    @Override
    public void close() {
        // nop
    }

}
