package com.dfi.sbc2ha.sensor.temperature.oneWire;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.dfi.sbc2ha.helper.bus.BusFactory;
import com.diozero.devices.oneWire.OneWireThermSensor;
import com.diozero.devices.oneWire.bus.DS248.DS2482BusImpl;
import com.diozero.devices.oneWire.bus.DS248.DS2482ThermDevice;
import com.diozero.devices.oneWire.bus.OneWireDevice;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;
import com.diozero.devices.oneWire.bus.fs.FsBus;
import com.diozero.devices.oneWire.bus.fs.FsThermDevice;
import com.diozero.util.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;

public class OneWireFactory {
    public static W1TempSensor createOneWireTherm(SensorConfig config, Map<String, Object> busMap) {
        switch (config.getPlatform()) {
            case DALLAS:
                return createOneWireThermFs(config, busMap);
            default:
            case DS2482:
                return createOneWireThermDs2482(config, busMap);
        }
    }

    private static W1TempSensor createOneWireThermDs2482(SensorConfig config, Map<String, Object> busMap) {
        DS2482BusImpl bus = BusFactory.getBus(config.getBusId(), busMap, DS2482BusImpl.class);
        String longStr = ((DS18B20busDS2482) config).getAddress();
        long longAddress;
        try {
            longAddress = Long.parseLong(longStr);
        } catch (RuntimeException e) {
            BigInteger bigInteger = new BigInteger(longStr);
            longAddress = bigInteger.longValue();

        }
        TemperatureContainer container = (TemperatureContainer) bus.getContainer(longAddress);
        DS2482ThermDevice device = new DS2482ThermDevice(container);
        OneWireThermSensor delegate = new OneWireThermSensor(device);

        try {
            device.setTemperatureResolution(0.5d);
        } catch (OneWireException e) {
            throw new RuntimeException(e);
        }

        return new W1TempSensor(delegate, config.getId(), config.getUpdateInterval());

    }

    private static W1TempSensor createOneWireThermFs(SensorConfig config, Map<String, Object> busMap) {
        if (!(config instanceof DS18B20busFs)) {
            throw new IllegalArgumentException("bad config type: " + config.getClass().getCanonicalName() + " expected:" + DS18B20busFs.class.getCanonicalName());
        }

        FsBus bus = BusFactory.getBus(config.getBusId(), busMap, FsBus.class);

        //TODO refactor
        String longStr = ((DS18B20busFs) config).getAddress();
        long longAddress = Long.parseLong(longStr);
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
