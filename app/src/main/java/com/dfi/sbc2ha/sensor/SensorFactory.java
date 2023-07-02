package com.dfi.sbc2ha.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputSwitchConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.AdcSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.exception.MissingHardwareException;
import com.dfi.sbc2ha.helper.ConfigurePin;
import com.dfi.sbc2ha.sensor.analog.AnalogSensor;
import com.dfi.sbc2ha.sensor.binary.Binary;
import com.dfi.sbc2ha.sensor.binary.BinarySensor;
import com.dfi.sbc2ha.sensor.binary.Button;
import com.dfi.sbc2ha.sensor.binary.ButtonState;
import com.dfi.sbc2ha.sensor.temperature.Lm75TempSensor;
import com.dfi.sbc2ha.sensor.temperature.oneWire.OneWireFactory;
import com.dfi.sbc2ha.sensor.temperature.oneWire.W1TempSensor;
import com.diozero.api.GpioPullUpDown;
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
            Logger.error("error creating " + config.getPin());
            throw new RuntimeException(e);
        }
    }

    public static W1TempSensor createOneWireTherm(SensorConfig config, Map<String, Object> busMap) {
        return OneWireFactory.createOneWireTherm(config, busMap);
    }


    public static Lm75TempSensor createLM75Temp(Lm75SensorConfig config) {
        //config.setUpdateInterval(DurationStyle.detectAndParse("5s"));

        return Lm75TempSensor.Builder.builder(Integer.parseInt(config.getAddress()))
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
        PinInfo pinInfo = searchPinInfo(config.getPin());
        Button.Builder builder = Button.Builder.builder(pinInfo)
                .setClickDetection(ButtonState.valueOf(config.getClickDetection().name()))
                .setDebounceTimeMillis(config.getBounceTime().toMillis());

        return createBinarySensor(config, pinInfo, builder).build();


    }

    private static Binary createBinary(InputSensorConfig config) throws MissingHardwareException {
        PinInfo pinInfo = searchPinInfo(config.getPin());
        Binary.Builder builder = Binary.Builder.builder(pinInfo)
                .setDebounceTimeMillis(config.getBounceTime().toMillis());

        return createBinarySensor(config, pinInfo, builder).build();
    }

    public static AnalogSensor createAnalogSensor(AdcSensorConfig config) {
        try {
            PinInfo pinInfo = searchPinInfo(config.getPin());
            AnalogSensor.Builder builder = AnalogSensor.Builder.builder(pinInfo);
            builder.setName(Optional.of(config.getId()).orElse(config.getId()));

            return builder.build();

        } catch (InvalidModeException | MissingHardwareException e) {
            Logger.error("error creating " + config.getPin());
            throw new RuntimeException(e);
        }

    }

    private static <T extends BinarySensor.Builder<T>> T createBinarySensor(InputConfig<?> config, PinInfo pinInfo, T builder) {
        if (config.getGpioMode() != null) {
            GpioPullUpDown mode = findPullUpDown(config.getGpioMode());
            if (mode != null) {
                builder.setPullUpDown(mode);
                ConfigurePin.configure(pinInfo, mode);
            }
        }
        builder.setName(Optional.of(config.getId()).orElse(config.getId()));
        builder.setInverted(config.isInverted());

        return builder;

    }

    private static void registerLoggerEvents(BinarySensor<?, ?> input, InputConfig<?> config) {
        input.whenAny(evt -> Logger.info("{} on:{} pin:{} time: {} ", evt.getState(), input.getName(), config.getPin(), evt.getNanoTime()));
    }

    public static PinInfo searchPinInfo(String headerPin) throws MissingHardwareException {
        BoardInfo boardInfo = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
        String delimiter = "_";
        String[] temp = headerPin.split(delimiter);
        if (temp.length != 2) {
            throw new IllegalArgumentException(headerPin);
        }

        return getPinInfoFromHeader(boardInfo, temp[0], temp[1]);
    }

    private static PinInfo getPinInfoFromHeader(BoardInfo boardInfo, String headerName, String pinNumber) throws MissingHardwareException {
        Map<Integer, PinInfo> headerMap = getHeaderMap(boardInfo, headerName);
        int key = Integer.parseInt(pinNumber);
        if (headerMap.containsKey(key)) {
            return headerMap.get(key);
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
