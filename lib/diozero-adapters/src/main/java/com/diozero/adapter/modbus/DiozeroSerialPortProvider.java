package com.diozero.adapter.modbus;

public class DiozeroSerialPortProvider implements net.solarnetwork.io.modbus.serial.SerialPortProvider {
    @Override
    public net.solarnetwork.io.modbus.serial.SerialPort getSerialPort(String name) {
        return new DiozeroSerialPortAdapter(name);
    }
}
