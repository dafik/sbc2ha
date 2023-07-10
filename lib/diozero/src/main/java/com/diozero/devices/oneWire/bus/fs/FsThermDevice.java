package com.diozero.devices.oneWire.bus.fs;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;


/**
 * @see <a href="https://www.maximintegrated.com/en/products/analog/sensors-and-sensor-interface/DS18B20.html">DS18B20 Programmable Resolution 1-Wire Digital Thermometer</a>
 * @see <a href="https://learn.adafruit.com/adafruits-raspberry-pi-lesson-11-ds18b20-temperature-sensing?view=all">Adafruit's Raspberry Pi Lesson 11. DS18B20 Temperature Sensing</a>
 * @see <a href="https://github.com/timofurrer/w1thermsensor">W1ThermSensor Python library</a>
 */
public class FsThermDevice extends FsDevice implements OneWireThermDevice {
    public FsThermDevice(FsBusImpl bus, Type type, String serialNumber) {
        super(bus, type, serialNumber);
    }

    /**
     * Get temperature in degrees celsius
     *
     * @return Temperature (deg C)
     * @throws RuntimeIOException if an I/O error occurs
     */
    public float getTemperature() throws RuntimeIOException {
        return oneWireBus.readTemperature(type, serialNumber);
    }
}
