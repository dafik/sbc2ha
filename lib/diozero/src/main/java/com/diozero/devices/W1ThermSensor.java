package com.diozero.devices;



import com.diozero.devices.oneWire.OneWireThermSensor;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;
import com.diozero.devices.oneWire.bus.fs.FsBusImpl;

import java.util.List;
import java.util.stream.Collectors;


public class W1ThermSensor extends OneWireThermSensor {

    public W1ThermSensor(OneWireThermDevice device) {
        super(device);

    }

    /**
     * Compatibility with previous implementation without bus
     *
     * @deprecated use  OneWireBus.getAvailableSensors()
     */
    @Deprecated
    public static List<W1ThermSensor> getAvailableSensors() {
        return getAvailableSensors(FsBusImpl.BASE_DIRECTORY);
    }

    /**
     * Compatibility with previous implementation without bus
     *
     * @deprecated use  OneWireFsBus(folder).getAvailableSensors()
     */
    @Deprecated
    public static List<W1ThermSensor> getAvailableSensors(String folder) {
        return FsBusImpl.getAvailableSensors(folder).stream()
                .map(s -> (W1ThermSensor) s)
                .collect(Collectors.toList());
    }


}
