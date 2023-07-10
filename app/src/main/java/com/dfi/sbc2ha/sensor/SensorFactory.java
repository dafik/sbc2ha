package com.dfi.sbc2ha.sensor;

import com.dfi.sbc2ha.bus.Lm75Bus;
import com.dfi.sbc2ha.bus.OWireBus;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.AdcSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSwitchConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import com.dfi.sbc2ha.exception.MissingHardwareException;
import com.dfi.sbc2ha.helper.ConfigurePin;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionInputBoard;
import com.dfi.sbc2ha.sensor.analog.AnalogSensor;
import com.dfi.sbc2ha.sensor.binary.Binary;
import com.dfi.sbc2ha.sensor.binary.BinarySensor;
import com.dfi.sbc2ha.sensor.binary.Button;
import com.dfi.sbc2ha.sensor.binary.ButtonState;
import com.dfi.sbc2ha.sensor.temperature.Lm75TempSensor;
import com.dfi.sbc2ha.sensor.temperature.oneWire.OneWireFactory;
import com.dfi.sbc2ha.sensor.temperature.oneWire.W1TempSensor;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.I2CDevice;
import com.diozero.api.InvalidModeException;
import com.diozero.api.PinInfo;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;

public class SensorFactory {

    //InputSwitchConfig
    public static BinarySensor<? extends StateEvent<?>, ?> createBinarySensor(InputConfig<?> config) throws RuntimeException {
        try {
            return create(config);
        } catch (InvalidModeException | MissingHardwareException e) {
            Logger.error("error creating " + config.getInput());
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
            Logger.error("error creating header: {} pin: {}", headerName, pinNumber);
            throw new RuntimeException(e);
        }
    }


    public static W1TempSensor createOneWireTherm(DS18B20 config, OWireBus bus) {
        return OneWireFactory.createOneWireTherm(config, bus);
    }


    public static Lm75TempSensor createLM75Temp(Lm75SensorConfig config, Lm75Bus busMap) {
        I2CDevice busInternal = busMap.getBus();
        return Lm75TempSensor.Builder.builder(busInternal)
                .setName(config.getId())
                .setUpdateInterval(config.getUpdateInterval())
                .setFilters(config.getFilters())
                .build();
    }


    private static BinarySensor<? extends StateEvent<?>, ?> create(InputConfig<?> config) throws MissingHardwareException {
        BinarySensor<?, ?> sensor;
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

    public static AnalogSensor createAnalogSensor(AdcSensorConfig config) {
        try {
            ExtensionInputBoard.InputDefinition inputDefinition = getAnalogDefinition(config.getAnalog());
            PinInfo pinInfo = searchPinInfo(inputDefinition);
            AnalogSensor.Builder builder = AnalogSensor.Builder.builder(pinInfo);
            builder.setName(Optional.of(config.getId()).orElse(config.getId()));

            return builder.build();

        } catch (InvalidModeException | MissingHardwareException e) {
            Logger.error("error creating analog" + config.getAnalog());
            throw new RuntimeException(e);
        }

    }

    private static <T extends BinarySensor.Builder<T>> T createBinarySensor(InputConfig<?> config, PinInfo pinInfo, T builder, boolean isPullUp) {
        if (isPullUp) {
            GpioPullUpDown mode = GpioPullUpDown.PULL_UP;
            builder.setPullUpDown(mode);
            ConfigurePin.configure(pinInfo, mode);
        }
        builder.setName(Optional.of(config.getId()).orElse(config.getId()));
        builder.setInverted(config.isInverted());

        return builder;

    }

    private static void registerLoggerEvents(BinarySensor<?, ?> input, InputConfig<?> config) {
        input.whenAny(evt -> Logger.info("{} on:{} pin:{} time: {} ", evt.getState(), input.getName(), config.getInput(), evt.getNanoTime()));
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
}
