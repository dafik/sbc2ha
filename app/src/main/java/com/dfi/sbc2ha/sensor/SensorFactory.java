package com.dfi.sbc2ha.sensor;

import com.dfi.sbc2ha.bus.Bus;
import com.dfi.sbc2ha.bus.Lm75Bus;
import com.dfi.sbc2ha.bus.ModbusBus;
import com.dfi.sbc2ha.bus.OWireBus;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SingleSourceConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.AnalogSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.NtcConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.ResistanceConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSwitchConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import com.dfi.sbc2ha.exception.MissingHardwareException;
import com.dfi.sbc2ha.helper.ConfigurePin;
import com.dfi.sbc2ha.helper.UnitConverter;
import com.dfi.sbc2ha.helper.bus.ModbusFactory;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionInputBoard;
import com.dfi.sbc2ha.modbus.Modbus;
import com.dfi.sbc2ha.sensor.analog.AnalogSensor;
import com.dfi.sbc2ha.sensor.analog.NtcSensor;
import com.dfi.sbc2ha.sensor.analog.ResistanceSensor;
import com.dfi.sbc2ha.sensor.binary.Binary;
import com.dfi.sbc2ha.sensor.binary.Button;
import com.dfi.sbc2ha.state.sensor.ButtonState;
import com.dfi.sbc2ha.sensor.modbus.ModbusSensor;
import com.dfi.sbc2ha.sensor.temperature.Lm75TempSensor;
import com.dfi.sbc2ha.sensor.temperature.oneWire.OneWireFactory;
import com.dfi.sbc2ha.sensor.temperature.oneWire.W1TempSensor;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.I2CDevice;
import com.diozero.api.InvalidModeException;
import com.diozero.api.PinInfo;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SensorFactory {

    public static Sensor create(SensorConfig sensorConfig, Map<String, Sensor> sensorMap, List<Bus<?>> busMap) {
        if (sensorConfig instanceof AnalogSensorConfig) {
            return createAnalogSensor((AnalogSensorConfig) sensorConfig);
        } else if (sensorConfig instanceof SingleSourceConfig) {
            Sensor source = sensorMap.get(((SingleSourceConfig) sensorConfig).getSensor());
            if (sensorConfig instanceof ResistanceConfig) {
                return createResistanceSensor((ResistanceConfig) sensorConfig, (AnalogSensor) source);
            } else if (sensorConfig instanceof NtcConfig) {
                return createNtcSensor((NtcConfig) sensorConfig, (ResistanceSensor) source);
            }
        } else if (sensorConfig instanceof DS18B20) {
            OWireBus bus = getBus(busMap, sensorConfig.getPlatform(), ((DS18B20) sensorConfig).getBusId(), OWireBus.class);
            return SensorFactory.createOneWireTherm(((DS18B20) sensorConfig), bus);
        } else if (sensorConfig instanceof Lm75SensorConfig) {
            Lm75Bus bus = getBus(busMap, PlatformType.LM75, ((Lm75SensorConfig) sensorConfig).getBusId(), Lm75Bus.class);
            return createLM75Temp((Lm75SensorConfig) sensorConfig, bus);
        } else if (sensorConfig instanceof ModbusSensorConfig) {
            ModbusBus bus = getBus(busMap, PlatformType.MODBUS, ((ModbusSensorConfig) sensorConfig).getBusId(), ModbusBus.class);
            return createModbusDevice((ModbusSensorConfig) sensorConfig, bus);
        } else if (sensorConfig instanceof InputConfig) {
            return createBinarySensor((InputConfig) sensorConfig);
        }
        throw new IllegalArgumentException("unknown sensor sensorConfig " + sensorConfig.getClass().getSimpleName());
    }


    //InputSwitchConfig
    public static BinarySensor createBinarySensor(InputConfig config) throws RuntimeException {
        try {
            return create(config);
        } catch (InvalidModeException | MissingHardwareException e) {
            log.error("error creating " + config.getInput());
            throw new RuntimeException(e);
        }
    }

    public static Button createCaseButton(String headerName, int pinNumber, boolean pullUp) throws RuntimeException {
        try {
            BoardInfo boardInfo = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
            PinInfo pinInfo = getPinInfoFromHeader(boardInfo, headerName, pinNumber);
            Button.Builder builder = Button.Builder.builder(pinInfo)
                    .setClickDetection(ButtonState.SINGLE)
                    .setDebounceTimeMillis(DurationStyle.detectAndParse("25ms").toMillis())
                    .setPud(pullUp ? GpioPullUpDown.PULL_UP : GpioPullUpDown.NONE);

            return builder.build();
        } catch (InvalidModeException | MissingHardwareException e) {
            log.error("error creating header: {} pin: {}", headerName, pinNumber);
            throw new RuntimeException(e);
        }
    }


    public static W1TempSensor createOneWireTherm(DS18B20 config, OWireBus bus) {
        return OneWireFactory.createOneWireTherm(config, bus);
    }


    public static Lm75TempSensor createLM75Temp(Lm75SensorConfig config, Lm75Bus busMap) {
        I2CDevice busInternal = busMap.getBus();
        return Lm75TempSensor.Builder.builder(busInternal)
                .setName(config.getName())
                .setUpdateInterval(config.getUpdateInterval())
                .setFilters(config.getFilters())
                .build();
    }

    public static ModbusSensor createModbusDevice(ModbusSensorConfig config, ModbusBus bus) {

        Modbus busInternal = bus.getBus();
        ModbusSensorDefinition def = ModbusFactory.getDevice(config.getModel());

        return new ModbusSensor(
                config.getName(),
                config.getName().replace(" ", ""),
                config.getAddress(),
                config.getUpdateInterval(),
                busInternal,
                def);

    }


    private static BinarySensor create(InputConfig config) throws MissingHardwareException {
        BinarySensor sensor;
        switch (config.getKind()) {
            case SWITCH:
                sensor = createButton((InputSwitchConfig) config);
                break;
            case SENSOR:
            default:
                sensor = createBinary((InputSensorConfig) config);
        }
        registerLoggerEvents(sensor, config);

        return sensor;
    }

    private static Button createButton(InputSwitchConfig config) throws MissingHardwareException {
        ExtensionInputBoard.InputDefinition inputDefinition = getInputDefinition(config.getInput());
        PinInfo pinInfo = searchPinInfo(inputDefinition);
        Button.Builder builder = Button.Builder.builder(pinInfo)
                .setClickDetection(ButtonState.valueOf(config.getClickDetection().name()))
                .setDebounceTimeMillis(config.getBounceTime().toMillis());

        return createBinarySensor(config, pinInfo, builder, inputDefinition.isPullUp()).build();


    }

    private static Binary createBinary(InputSensorConfig config) throws MissingHardwareException {
        ExtensionInputBoard.InputDefinition inputDefinition = getInputDefinition(config.getInput());
        PinInfo pinInfo = searchPinInfo(inputDefinition);
        Binary.Builder builder = Binary.Builder.builder(pinInfo)
                .setDebounceTimeMillis(config.getBounceTime().toMillis());

        return createBinarySensor(config, pinInfo, builder, inputDefinition.isPullUp()).build();
    }

    public static AnalogSensor createAnalogSensor(AnalogSensorConfig config) {
        try {
            ExtensionInputBoard.InputDefinition inputDefinition = getAnalogDefinition(config.getAnalog());
            PinInfo pinInfo = searchPinInfo(inputDefinition);
            AnalogSensor.Builder builder = AnalogSensor.Builder.builder(pinInfo);
            builder.setName(config.getName());
            builder.setFilters(config.getFilters());
            builder.setUpdateInterval(config.getUpdateInterval());

            return builder.build();

        } catch (InvalidModeException | MissingHardwareException e) {
            log.error("error creating analog" + config.getAnalog());
            throw new RuntimeException(e);
        }

    }

    public static ResistanceSensor createResistanceSensor(ResistanceConfig config, AnalogSensor source) {
        float resistor = (float) UnitConverter.resistance(config.getResistor());
        ResistanceSensor.Builder builder = ResistanceSensor.Builder
                .builder(source, config.getDirection(), resistor)
                .setName(config.getName())
                .setFilters(config.getFilters());

        if (config.getReferenceVoltage() != null) {
            float referenceVoltage = (float) UnitConverter.voltage(config.getReferenceVoltage());
            builder.setRange(referenceVoltage);
        }


        return builder.build();
    }

    public static NtcSensor createNtcSensor(NtcConfig config, ResistanceSensor source) {
        NtcSensor.Builder builder;
        if (config.getBCalibration() != null) {
            builder = NtcSensor.Builder
                    .builder(source, config.getBCalibration());
        } else if (config.getVCalibration() != null) {
            builder = NtcSensor.Builder
                    .builder(source, config.getVCalibration());
        } else {
            throw new IllegalArgumentException("bConstant nor value calibration not found");
        }
        builder.setName(config.getName())
                .setFilters(config.getFilters());

        return builder.build();
    }

    private static <T extends BinarySensor.Builder<T>> T createBinarySensor(InputConfig config, PinInfo pinInfo, T builder, boolean isPullUp) {
        if (isPullUp) {
            GpioPullUpDown mode = GpioPullUpDown.PULL_UP;
            builder.setPullUpDown(mode);
            ConfigurePin.configure(pinInfo, mode);
        }
        builder.setName(Optional.of(config.getName()).orElse(config.getName()));
        builder.setInverted(config.isInverted());

        return builder;

    }

    private static void registerLoggerEvents(BinarySensor sensor, InputConfig config) {
        sensor.whenAny(evt -> {
            log.info("{} id:{},{} time: {} ", evt.getState(), config.getInput(), sensor.getName(), evt.getEpochTime());
        });
    }

    public static PinInfo searchPinInfo(String headerPin) throws MissingHardwareException {
        BoardInfo boardInfo = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
        String delimiter = "_";
        String[] temp = headerPin.split(delimiter);
        if (temp.length != 2) {
            throw new IllegalArgumentException(headerPin);
        }

        return getPinInfoFromHeader(boardInfo, temp[0], Integer.parseInt(temp[1]));
    }

    public static PinInfo searchPinInfo(ExtensionInputBoard.InputDefinition def) throws MissingHardwareException {
        BoardInfo boardInfo = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
        return getPinInfoFromHeader(boardInfo, def.getHeader(), def.getPin());
    }

    private static ExtensionInputBoard.InputDefinition getInputDefinition(int input) {
        return ExtensionBoardInfo.getInstance().getIn().get("D").get(input);
    }

    private static ExtensionInputBoard.InputDefinition getAnalogDefinition(int input) {
        return ExtensionBoardInfo.getInstance().getIn().get("A").get(input);
    }

    private static PinInfo getPinInfoFromHeader(BoardInfo boardInfo, String headerName, int pinNumber) throws MissingHardwareException {
        Map<Integer, PinInfo> headerMap = getHeaderMap(boardInfo, headerName);
        if (headerMap.containsKey(pinNumber)) {
            return headerMap.get(pinNumber);
        }
        throw new MissingHardwareException("Pin: " + pinNumber + " not found in header: " + headerName);
    }

    private static Map<Integer, PinInfo> getHeaderMap(BoardInfo boardInfo, String name) throws MissingHardwareException {
        Map<String, Map<Integer, PinInfo>> headers = boardInfo.getHeaders();
        if (headers.containsKey(name)) {
            return headers.get(name);
        }
        throw new MissingHardwareException("Header: " + name + " not found");
    }

    private static GpioPullUpDown findPullUpDown(String mode) {
        switch (mode) {
            case "gpio_pd":
                return GpioPullUpDown.PULL_DOWN;
            case "gpio_pu":
                return GpioPullUpDown.PULL_UP;
            default:
                return GpioPullUpDown.NONE;
        }
    }

    private static <B extends Bus<?>> B getBus(List<Bus<?>> busMap, PlatformType type, String id, Class<B> returnClass) {
        return busMap.stream()
                .filter(bus -> bus.getPlatformType() == type)
                .filter(bus -> bus.getId().equals(id))
                .map(returnClass::cast)
                .findFirst()
                .orElseThrow();
    }
}
