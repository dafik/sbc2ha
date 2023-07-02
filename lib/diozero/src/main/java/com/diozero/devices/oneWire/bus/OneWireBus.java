package com.diozero.devices.oneWire.bus;

import com.diozero.devices.oneWire.OneWireSensor;
import com.diozero.devices.oneWire.OneWireThermSensor;

import java.util.List;

public interface OneWireBus {
    List<OneWireThermSensor> getAvailableThermSensors();
    List<OneWireSensor> getAvailableSensors();
}
