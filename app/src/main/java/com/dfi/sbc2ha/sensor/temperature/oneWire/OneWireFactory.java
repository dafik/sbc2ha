package com.dfi.sbc2ha.sensor.temperature.oneWire;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.dfi.sbc2ha.bus.OWireBus;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.diozero.devices.oneWire.OneWireThermSensor;
import com.diozero.devices.oneWire.bus.DS248.DS2482BusImpl;
import com.diozero.devices.oneWire.bus.DS248.DS2482ThermDevice;
import com.diozero.devices.oneWire.bus.OneWireDevice;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;
import com.diozero.devices.oneWire.bus.fs.FsBusImpl;
import com.diozero.devices.oneWire.bus.fs.FsThermDevice;
import com.diozero.util.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class OneWireFactory {
    public static W1TempSensor createOneWireTherm(DS18B20 config, OWireBus bus) {
        switch (config.getPlatform()) {
            case DALLAS:
                return createOneWireThermFs((DS18B20busFs) config, bus);
            default:
            case DS2482:
                return createOneWireThermDs2482((DS18B20busDS2482) config, bus);
        }
    }

    private static W1TempSensor createOneWireThermDs2482(DS18B20busDS2482 config, OWireBus busMap) {
        DS2482BusImpl busInternal = (DS2482BusImpl) busMap.getBus();

        BigInteger bigInteger = config.getAddress();
        long longAddress = bigInteger.longValue();


        TemperatureContainer container = (TemperatureContainer) busInternal.getContainer(longAddress);
        DS2482ThermDevice device = new DS2482ThermDevice(container);
        OneWireThermSensor delegate = new OneWireThermSensor(device);

        try {
            device.setTemperatureResolution(0.5d);
        } catch (OneWireException e) {
            throw new RuntimeException(e);
        }

        return new W1TempSensor(delegate, config.getId(), config.getUpdateInterval());

    }

    private static W1TempSensor createOneWireThermFs(DS18B20busFs config, OWireBus busMap) {

        FsBusImpl bus = (FsBusImpl) busMap.getBus();

        //TODO refactor
        BigInteger address = config.getAddress();
        long longAddress = address.longValue();
        //String strAddress = String.format("%X", longAddress);
        String strAddress = Hex.encodeHexString(longToBytes(longAddress)).toLowerCase();
        String deviceAddress = strAddress.substring(0, strAddress.length() - 2);
        String typeStr = strAddress.substring(strAddress.length() - 2);
        Integer id = Integer.valueOf(typeStr, 16);
        OneWireDevice.Type type = OneWireDevice.Type.valueOf(id);
        if (type != OneWireDevice.Type.DS18B20) {
            throw new IllegalArgumentException("bad device type: " + type + " expected:" + OneWireDevice.Type.DS18B20);
        }

        OneWireThermDevice device = new FsThermDevice(bus, OneWireDevice.Type.DS18B20, deviceAddress);
        OneWireThermSensor delegate = new OneWireThermSensor(device);

        return new W1TempSensor(delegate, config.getId(), config.getUpdateInterval());

    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}
