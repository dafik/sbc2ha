package com.dfi.sbc2ha.helper.bus;

import com.diozero.adapter.onewire.DiozeroDS2482Adapter;
import com.diozero.adapter.onewire.DiozeroI2CAdapter;
import com.diozero.api.I2CDevice;
import com.diozero.devices.oneWire.bus.DS248.DS2482BusImpl;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class DS2482Factory {

    public static DS2482BusImpl setupDS2482Bus(I2CDevice i2CDevice) {
        DiozeroI2CAdapter i2CAdapter = new DiozeroI2CAdapter(i2CDevice);
        DiozeroDS2482Adapter adapter = new DiozeroDS2482Adapter(i2CAdapter);

        DS2482BusImpl ds2482Bus = new DS2482BusImpl(adapter);
        ds2482Bus.getAvailableSensors();
        return ds2482Bus;

    }
}
