package com.dfi.sbc2ha.helper.bus;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.OneWireContainer28;
import com.diozero.adapter.onewire.DiozeroDS2482Adapter;
import com.diozero.adapter.onewire.DiozeroI2CAdapter;
import com.diozero.adapter.onewire.OneWireContainer28M;
import com.diozero.api.I2CDevice;
import com.diozero.devices.oneWire.bus.DS248.DS2482BusImpl;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class DS2482Factory {
    public static void setupDS2482(I2CDevice i2CDevice) {
        DiozeroI2CAdapter i2CAdapter = new DiozeroI2CAdapter(i2CDevice);
        DiozeroDS2482Adapter adapter = new DiozeroDS2482Adapter(i2CAdapter);

        List<OneWireContainer28> devices = new ArrayList<>();

        boolean firstDevice = adapter.findFirstDevice();
        while (firstDevice) {

            //OneWireContainer deviceContainer = adapter.getDeviceContainer();

            byte[] address = new byte[8];
            adapter.getAddress(address);
            OneWireContainer28M container = new OneWireContainer28M(adapter, address);
            devices.add(container);

            Logger.info("found {}", container.getAddressAsString());


            firstDevice = adapter.findNextDevice();
        }

        for (OneWireContainer28 container : devices) {
            try {
                byte[] state = container.readDevice();

                container.setTemperatureResolution(0.125, state);
                container.writeDevice(state);
            } catch (OneWireException e) {
                throw new RuntimeException(e);
            }
        }

        //while (true) {
        for (OneWireContainer28 container : devices) {
            try {
                byte[] state = new byte[9];
                container.setTemperatureResolution(0.125, state);
                container.doTemperatureConvert(state);
                state = container.readDevice();
                double temperature = container.getTemperature(state);
                Logger.info("temp {}: {}", temperature, container.getAddressAsString());


            } catch (OneWireException e) {
                throw new RuntimeException(e);
            }
        }
        //}
    }

    public static DS2482BusImpl setupDS2482Bus(I2CDevice i2CDevice) {
        DiozeroI2CAdapter i2CAdapter = new DiozeroI2CAdapter(i2CDevice);
        DiozeroDS2482Adapter adapter = new DiozeroDS2482Adapter(i2CAdapter);

        DS2482BusImpl ds2482Bus = new DS2482BusImpl(adapter);
        ds2482Bus.getAvailableSensors();
        return ds2482Bus;

    }
}
