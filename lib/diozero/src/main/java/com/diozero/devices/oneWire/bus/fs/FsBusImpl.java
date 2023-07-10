package com.diozero.devices.oneWire.bus.fs;

import com.diozero.api.NoSuchDeviceException;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.oneWire.OneWireGenericSensor;
import com.diozero.devices.oneWire.OneWireSensor;
import com.diozero.devices.oneWire.OneWireThermSensor;
import com.diozero.devices.oneWire.bus.OneWireDevice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FsBusImpl implements FsBus {
    public static final String BASE_DIRECTORY = "/sys/bus/w1/devices";
    public static final String SLAVE_FILE = "w1_slave";
    private final String baseDirectory;

    public FsBusImpl() {
        this(BASE_DIRECTORY);
    }

    public FsBusImpl(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public static List<OneWireThermSensor> getAvailableSensors(String folder) {
        try (FsBusImpl adapter = new FsBusImpl(folder)) {
            return adapter.getAvailableThermSensors();
        }
    }

    private OneWireSensor createDevice(Path path) {
        String serialNumber = path.toFile().getName().split("-")[1];
        OneWireDevice.Type type;
        try {
            type = OneWireDevice.Type.valueOf(path);

        } catch (IllegalArgumentException e) {
            type = OneWireDevice.Type.GENERIC;
        }

        switch (type) {
            case DS18S20:
            case DS1822:
            case DS18B20:
            case DS1825:
            case DS28EA00:
            case MAX31850K:
                FsThermDevice device = new FsThermDevice(this, type, serialNumber);
                return new OneWireThermSensor(device);

            case GENERIC:
            default:
                FsDevice fsDevice = new FsDevice(this, type, serialNumber);
                return new OneWireGenericSensor(fsDevice);
        }
    }

    @Override
    public List<OneWireThermSensor> getAvailableThermSensors() {
        Path sensor_path = Paths.get(baseDirectory);
        Predicate<Path> is_sensor = path -> path.toFile().isDirectory() && OneWireDevice.Type.isValid(path);
        try (Stream<Path> list = Files.list(sensor_path)) {
            return list.filter(is_sensor).map(path -> (OneWireThermSensor) createDevice(path)).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public List<OneWireSensor> getAvailableSensors() {
        Path sensor_path = Paths.get(baseDirectory);
        Predicate<Path> is_sensor = path -> path.toFile().isDirectory();
        try (Stream<Path> list = Files.list(sensor_path)) {
            return list.filter(is_sensor).map(this::createDevice).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public float readTemperature(OneWireDevice.Type type, String serialNumber) {
        Path slaveFilePath = Path.of(baseDirectory, type + "-" + serialNumber).resolve(SLAVE_FILE);

        /*
         * 53 01 4b 46 7f ff 0d 10 e9 : crc=e9 YES
         * 53 01 4b 46 7f ff 0d 10 e9 t=21187
         * (?s).*crc=[0-9a-f]+ ([A-Z]+).*t=([0-9]+)
         */
        try {
            List<String> lines = Files.readAllLines(slaveFilePath);
            if (!lines.get(0).trim().endsWith("YES")) {
                throw new RuntimeIOException("1-wire slave not ready, serial='" + serialNumber + "'");
            }

            return Float.parseFloat(lines.get(1).split("=")[1]) / 1000;
        } catch (IOException e) {
            throw new RuntimeIOException("I/O error reading 1-wire slave, serial='" + serialNumber + "'");
        }
    }

    public void checkDeviceExist(OneWireDevice.Type type, String serialNumber) {
        Path path = Path.of(baseDirectory, Integer.toHexString(type.getId()) + "-" + serialNumber);
        if (!path.toFile().exists()) {
            throw new NoSuchDeviceException("No such 1-wire device #" + type + ":" + serialNumber);

        }
    }

    @Override
    public void close() throws RuntimeIOException {

    }
}
