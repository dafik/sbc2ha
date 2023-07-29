package com.diozero.devices.oneWire.bus.DS248;

import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.diozero.adapter.onewire.DiozeroDS2482Adapter;
import com.diozero.adapter.onewire.OneWireContainer28M;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.oneWire.OneWireGenericSensor;
import com.diozero.devices.oneWire.OneWireSensor;
import com.diozero.devices.oneWire.OneWireThermSensor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DS2482BusImpl implements DS2482Bus {

    private final DiozeroDS2482Adapter adapter;
    private List<OneWireContainer> devices = new ArrayList<>();

    public DS2482BusImpl(DiozeroDS2482Adapter adapter) {
        this.adapter = adapter;
        log.debug("1-wire adapter " + adapter.toString() + " created");
    }

    public OneWireContainer getContainer(long address) {
        return getDevices().stream()
                .filter(d -> d.getAddressAsLong() == address)
                .findFirst()
                .orElseThrow();
    }

    public OneWireContainer getContainer(double address) {
        return getDevices().stream()
                .filter(d -> d.getAddressAsLong() == address)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public List<OneWireSensor> getAvailableSensors() {

        return getDevices().stream()
                .map(container -> {
                    OneWireSensor oneWireSensor;
                    if (container instanceof TemperatureContainer) {
                        DS2482ThermDevice thermDevice = new DS2482ThermDevice((TemperatureContainer) container);
                        oneWireSensor = new OneWireThermSensor(thermDevice);

                    } else {
                        DS2482Device device = new DS2482Device((com.dalsemi.onewire.container.OneWireSensor) container);
                        oneWireSensor = new OneWireGenericSensor(device);
                    }
                    return oneWireSensor;

                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OneWireThermSensor> getAvailableThermSensors() {
        return getDevices().stream()
                .filter(container -> container instanceof TemperatureContainer)
                .map(container -> (TemperatureContainer) container)
                .map(DS2482ThermDevice::new)
                .map(OneWireThermSensor::new)
                .collect(Collectors.toList());
    }

    private List<OneWireContainer> getDevices() {
        if (devices.size() == 0) {
            search();
        }
        return devices;
    }

    private void search() {
        devices = new ArrayList<>();

        boolean firstDevice = adapter.findFirstDevice();
        while (firstDevice) {

            byte[] address = new byte[8];
            adapter.getAddress(address);
            int familyCode = address[0] & 127;

            String familyString = familyCode < 16 ?
                    ("0" + Integer.toHexString(familyCode)).toUpperCase() :
                    Integer.toHexString(familyCode).toUpperCase();

            OneWireContainer container = familyString.equals("28") ?
                    new OneWireContainer28M(adapter, address) :
                    adapter.getDeviceContainer();


            devices.add(container);

            log.info("found 1-Wire {} name: {}", container.getAddressAsString(), container.getName() + " device");

            firstDevice = adapter.findNextDevice();
        }
    }

    @Override
    public void close() throws RuntimeIOException {
        adapter.close();
    }
}
