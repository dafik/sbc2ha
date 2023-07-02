package com.dfi.sbc2ha.config.converter;

import com.dfi.sbc2ha.config.boneio.definition.BoneIoBMqttConfig;
import com.dfi.sbc2ha.config.boneio.definition.BoneIoConfig;
import com.dfi.sbc2ha.config.boneio.definition.BoneIoLoggerConfig;
import com.dfi.sbc2ha.config.boneio.definition.BoneIoOledConfig;
import com.dfi.sbc2ha.config.boneio.definition.action.BoneIoActionConfig;
import com.dfi.sbc2ha.config.boneio.definition.action.BoneIoCoverActionConfig;
import com.dfi.sbc2ha.config.boneio.definition.action.BoneIoMqttActionConfig;
import com.dfi.sbc2ha.config.boneio.definition.action.BoneIoOutputActionConfig;
import com.dfi.sbc2ha.config.boneio.definition.bus.BoneIoBusConfig;
import com.dfi.sbc2ha.config.boneio.definition.bus.BoneIoFsBusConfig;
import com.dfi.sbc2ha.config.boneio.definition.bus.BoneIoModbusBusConfig;
import com.dfi.sbc2ha.config.boneio.definition.input.BoneIoInputConfig;
import com.dfi.sbc2ha.config.boneio.definition.input.BoneIoInputSensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.input.BoneIoInputSwitchConfig;
import com.dfi.sbc2ha.config.boneio.definition.output.BoneIoGpioOutputConfig;
import com.dfi.sbc2ha.config.boneio.definition.output.BoneIoMcpOutputConfig;
import com.dfi.sbc2ha.config.boneio.definition.output.BoneIoOutputConfig;
import com.dfi.sbc2ha.config.boneio.definition.output.BoneIoPcaOutputConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoAdcSensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoLm75SensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoModbusSensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoSensorConfig;
import com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.ds2482.BoneIoDS18B20busDS2482;
import com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.fs.BoneIoDS18B20busFs;
import com.dfi.sbc2ha.config.sbc2ha.definition.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.ActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.CoverActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.MqttActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.OutputActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.BinarySensorDeviceClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SwitchDeviceClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.extentsionBoard.ExtensionBoardsConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.GpioOutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.McpOutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.PcaOutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BoneIoConverter {

    private static String inputBoard;
    private static String outputBoard;


    static AppConfig convert(BoneIoConfig boneIoConfig) {
        return convert(boneIoConfig,
                detectInputBoard(boneIoConfig.getInput()),
                detectOutputBoard(boneIoConfig.getOutput())
        );
    }

    static AppConfig convert(BoneIoConfig boneIoConfig, String inputBoard, String outputBoard) {

        AppConfig appConfig = new AppConfig();
        appConfig.setExtensionBoards(getExtensionBoardsConfig(inputBoard, outputBoard));

        appConfig.addPlatform(getMqttConfig(boneIoConfig.getMqtt()));
        appConfig.addPlatform(getOledConfig(boneIoConfig.getOled()));

        appConfig.addPlatforms(getBusConfig(boneIoConfig.getMcp23017(), PlatformType.MCP23XX));
        appConfig.addPlatforms(getBusConfig(boneIoConfig.getDs2482(), PlatformType.DS2482));
        appConfig.addPlatform(getDallas(boneIoConfig.getDallas()));
        appConfig.addPlatform(getModbus(boneIoConfig.getModbus()));

        appConfig.setActuator(setOutput(boneIoConfig.getOutput()));

        appConfig.addSensors(getLm75Config(boneIoConfig.getLm75(), appConfig));
        appConfig.addSensors(getModbusSensors(boneIoConfig.getModbusSensors()));
        appConfig.addSensors(getSensor(boneIoConfig.getSensor()));
        appConfig.addSensors(getAdc(boneIoConfig.getAdc()));

        appConfig.addSensors(getInput(boneIoConfig.getInput()));
        appConfig.setLogger(getLogger(boneIoConfig.getLogger()));

        return appConfig;
    }

    private static ExtensionBoardsConfig getExtensionBoardsConfig(String inputB, String outputB) {

        inputBoard = inputB;
        outputBoard = outputB;

        ExtensionBoardsConfig ebc = new ExtensionBoardsConfig();
        ebc.setVendor("boneio");
        ebc.setInputBoard(inputBoard);
        ebc.setOutputBoard(outputBoard);

        ExtensionBoardInfo.initialize(ebc.getVendor(), ebc.getInputBoard(), ebc.getOutputBoard());


        return ebc;
    }

    private static String detectInputBoard(List<BoneIoInputConfig<?>> inputList) {
        String inputBoard = "input-v0.3";

        Pattern reg = Pattern.compile("P9_(11|12|13)");
        for (var input : inputList) {
            if (reg.matcher(input.getPin()).matches()) {
                inputBoard = "input-v0.2";
                break;
            }
        }

        Logger.warn("guessing inputBoard: {}", inputBoard);

        return inputBoard;
    }

    private static String detectOutputBoard(List<BoneIoOutputConfig> outputList) {
        String outputBoard = "output-24x16A-v0.4";
        if (outputList.size() > 24) {
            outputBoard = "output-32x5A-v0.4";
        }

        Logger.warn("guessing outputBoard: {}", outputBoard);

        return outputBoard;
    }


    static List<PlatformConfig> getBusConfig(List<BoneIoBusConfig> l, PlatformType pt) {
        List<PlatformConfig> list = new ArrayList<>();
        for (var s : l) {
            I2cBusConfig d = new I2cBusConfig();
            d.setId(s.getId());
            d.setAddress(s.getAddress());
            d.setPlatform(pt);
        }
        return list;
    }

    static LoggerConfig getLogger(BoneIoLoggerConfig s) {
        LoggerConfig d = new LoggerConfig();
        d.setDefaultLevel(s.getDefaultLevel());
        Map<String, String> logs = new HashMap<>(s.getLogs());
        d.setLogs(logs);
        return d;
    }

    static List<SensorConfig> getAdc(List<BoneIoAdcSensorConfig> adc) {
        List<SensorConfig> l = new ArrayList<>();
        for (var s : adc) {
            AdcSensorConfig d = new AdcSensorConfig();
            d.setId(s.getId());
            d.setAnalog(detectAnalogInput(s.getPin()));

            d.setUpdateInterval(s.getUpdateInterval());
            d.setFilters(getFilters(s.getFilters()));
            l.add(d);
        }
        return l;
    }

    static List<SensorConfig> getInput(List<BoneIoInputConfig<?>> sl) {
        return sl.stream().map(s -> {
            switch (s.getKind()) {
                default:
                case SWITCH:
                    return getInputSwitchConfig((BoneIoInputSwitchConfig) s);
                case SENSOR:
                    return getInputSensorConfig((BoneIoInputSensorConfig) s);
            }
        }).collect(Collectors.toList());
    }

    static InputSensorConfig getInputSensorConfig(BoneIoInputSensorConfig s) {
        var d = new InputSensorConfig();
        d.setDeviceClass(convertEnum(BinarySensorDeviceClassType.class, s.getDeviceClass()));
        setInputConfig(s, d);

        var sa = s.getActions();
        Map<InputSensorAction, List<ActionConfig>> da = new HashMap<>();
        sa.forEach((san, sc) -> da.put(convertEnum(InputSensorAction.class, san), getActions(sc)));
        d.setActions(da);

        return d;
    }

    private static <S extends Enum<S>, D extends Enum<D>> D convertEnum(Class<D> destination, S value) {
        if (value == null) {
            return null;
        }

        try {
            return Enum.valueOf(destination, value.name());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    static InputSwitchConfig getInputSwitchConfig(BoneIoInputSwitchConfig s) {
        var d = new InputSwitchConfig();
        d.setDeviceClass(convertEnum(SwitchDeviceClassType.class, s.getDeviceClass()));
        setInputConfig(s, d);

        var sa = s.getActions();
        Map<InputSwitchAction, List<ActionConfig>> da = new HashMap<InputSwitchAction, List<ActionConfig>>();
        sa.forEach((san, sc) -> da.put(convertEnum(InputSwitchAction.class, san), getActions(sc)));
        d.setActions(da);

        return d;
    }

    static void setInputConfig(BoneIoInputConfig<?> s, InputConfig<?> d) {
        d.setId(s.getId());

        d.setInput(detectDigitalInput(s.getPin()));

        d.setClickDetection(convertEnum(ButtonState.class, s.getClickDetection()));
        d.setBounceTime(s.getBounceTime());
        d.setShowInHa(s.isShowInHa());
        d.setInverted(s.isInverted());
    }

    private static int detectDigitalInput(String pin) {
        return ExtensionBoardInfo.getInstance().getInputByPin("D", pin);
    }

    private static int detectAnalogInput(String pin) {
        return ExtensionBoardInfo.getInstance().getInputByPin("A", pin);
    }


    static List<ActionConfig> getActions(List<BoneIoActionConfig> sla) {
        List<ActionConfig> dla = new ArrayList<>();
        for (var sa : sla) {
            ActionConfig da;
            switch (sa.getAction()) {
                case MQTT:
                    da = getMqttAction((BoneIoMqttActionConfig) sa);
                    break;
                default:
                case OUTPUT:
                    da = getOutputAction((BoneIoOutputActionConfig) sa);
                    break;
                case COVER:
                    da = getCoverAction((BoneIoCoverActionConfig) sa);
                    break;
            }
            da.setAction(convertEnum(ActionType.class, sa.getAction()));
            dla.add(da);
        }
        return dla;
    }

    static MqttActionConfig getMqttAction(BoneIoMqttActionConfig s) {
        MqttActionConfig d = new MqttActionConfig();
        d.setTopic(s.getTopic());
        d.setActionMqttMsg(s.getActionMqttMsg());
        return d;
    }

    static OutputActionConfig getOutputAction(BoneIoOutputActionConfig s) {
        OutputActionConfig d = new OutputActionConfig();
        d.setActuatorId(s.getPin());
        return d;

    }

    static CoverActionConfig getCoverAction(BoneIoCoverActionConfig s) {
        CoverActionConfig d = new CoverActionConfig();
        d.setActuatorId(s.getPin());
        return d;
    }

    static List<ActuatorConfig> setOutput(List<BoneIoOutputConfig> sl) {
        return sl.stream().map(s -> {
            switch (s.getKind()) {
                case GPIO:
                    return getGpioOutput((BoneIoGpioOutputConfig) s);
                default:
                case MCP:
                    return getMcpOutput((BoneIoMcpOutputConfig) s);
                case PCA:
                    return getPcaOutput((BoneIoPcaOutputConfig) s);
            }
        }).collect(Collectors.toList());
    }

    static void setOutput(BoneIoOutputConfig s, ActuatorConfig d) {
        d.setId(s.getId());
        d.setOutput(detectOutput(s));
        d.setOutputType(convertEnum(OutputType.class, s.getOutputType()));
        d.setRestoreState(s.isRestoreState());
        d.setMomentaryTurnOff(s.getMomentaryTurnOff());
        d.setMomentaryTurnOn(s.getMomentaryTurnOn());
    }

    private static int detectOutput(BoneIoOutputConfig s) {
        ExtensionBoardInfo instance = ExtensionBoardInfo.getInstance();

        if (s instanceof BoneIoMcpOutputConfig) {
            return instance.getOutputMcpByPin(Integer.parseInt(s.getPin()),
                    Integer.parseInt(((BoneIoMcpOutputConfig) s).getMcpId().replace("mcp", "")));
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }

    static PcaOutputConfig getPcaOutput(BoneIoPcaOutputConfig s) {
        var d = new PcaOutputConfig();
        setOutput(s, d);
        d.setPcaId(s.getPcaId());
        d.setPercentageDefaultBrightnes(s.getPercentageDefaultBrightnes());
        return d;
    }

    static McpOutputConfig getMcpOutput(BoneIoMcpOutputConfig s) {
        var d = new McpOutputConfig();
        setOutput(s, d);
        d.setMcpId(s.getMcpId());

        return d;
    }

    static GpioOutputConfig getGpioOutput(BoneIoGpioOutputConfig s) {
        var d = new GpioOutputConfig();
        setOutput(s, d);

        return d;
    }

    static List<SensorConfig> getSensor(List<BoneIoSensorConfig> sl) {
        return sl.stream().map(s -> {
            switch (s.getPlatform()) {
                default:
                case DALLAS:
                    return getFsDs18B20((BoneIoDS18B20busFs) s);
                case DS2482:
                    return getDs2482Ds18B20((BoneIoDS18B20busDS2482) s);
            }
        }).collect(Collectors.toList());
    }

    static DS18B20busDS2482 getDs2482Ds18B20(BoneIoDS18B20busDS2482 s) {
        var d = new DS18B20busDS2482();
        d.setAddress(s.getAddress());
        d.setUnitOfMeasurement(s.getUnitOfMeasurement());
        setSensor(s, d);
        return d;
    }

    static DS18B20busFs getFsDs18B20(BoneIoDS18B20busFs s) {
        var d = new DS18B20busFs();
        d.setAddress(s.getAddress());
        d.setUnitOfMeasurement(s.getUnitOfMeasurement());
        setSensor(s, d);
        return d;
    }

    static void setSensor(BoneIoSensorConfig s, BusSensorConfig d) {
        d.setId(s.getId());
        d.setBusId(s.getBusId());
        d.setUpdateInterval(s.getUpdateInterval());
        d.setFilters(getFilters(s.getFilters()));
        d.setShowInHa(s.isShowInHa());
    }

    static List<SensorConfig> getModbusSensors(List<BoneIoModbusSensorConfig> sl) {
        return sl.stream().map(s -> {
            var d = new ModbusSensorConfig();
            d.setId(s.getId());
            d.setAddress(s.getAddress());
            d.setModel(s.getModel());
            d.setUpdateInterval(s.getUpdateInterval());

            return d;
        }).collect(Collectors.toList());
    }

    static ModbusBusConfig getModbus(BoneIoModbusBusConfig s) {
        if (s == null) return null;
        var d = new ModbusBusConfig();
        d.setId(s.getId());
        d.setUart(UartType.valueOf(s.getUart().name()));
        return null;
    }

    static FsBusConfig getDallas(BoneIoFsBusConfig s) {
        if (s == null) return null;
        var d = new FsBusConfig();
        d.setId(s.getId());
        return d;
    }

    static List<SensorConfig> getLm75Config(List<BoneIoLm75SensorConfig> lm75, AppConfig appConfig) {
        List<SensorConfig> list = new ArrayList<>();
        for (var s : lm75) {
            Lm75SensorConfig d = new Lm75SensorConfig();
            d.setId(s.getId());
            d.setUnitOfMeasurement(s.getUnitOfMeasurement());
            d.setUpdateInterval(s.getUpdateInterval());
            d.setFilters(getFilters(s.getFilters()));

            PlatformConfig platformConfig = appConfig.getPlatform(PlatformType.LM75).stream()
                    .filter(pc -> ((I2cBusConfig) pc).getAddress() == s.getAddress())
                    .findFirst()
                    .orElseGet(() -> createLm75Platfrom(appConfig, s));

            d.setBusId(platformConfig.getId());

            appConfig.getSensor().add(d);
        }
        return list;
    }

    private static PlatformConfig createLm75Platfrom(AppConfig appConfig, BoneIoLm75SensorConfig s) {
        I2cBusConfig pc = new I2cBusConfig();
        pc.setAddress(s.getAddress());
        pc.setId("Lm75");
        appConfig.addPlatform(pc);

        return pc;
    }

    static Map<String, String> getFilters(Map<String, String> filters) {
        return new HashMap<String, String>();
    }

    static OledConfig getOledConfig(BoneIoOledConfig oled) {
        OledConfig oledConfig = new OledConfig();
        oledConfig.setEnabled(oled.isEnabled());
        oledConfig.setScreensaverTimeout(oled.getScreensaverTimeout());
        oledConfig.setScreens(oled.getScreens().stream()
                .map(s -> convertEnum(ScreenType.class, s))
                .collect(Collectors.toList()));

        return oledConfig;
    }

    static MqttConfig getMqttConfig(BoneIoBMqttConfig mqtt) {
        MqttConfig mqttConfig = new MqttConfig();
        mqttConfig.setHost(mqtt.getHost());
        mqttConfig.setUsername(mqtt.getUsername());
        mqttConfig.setPassword(mqtt.getPassword());
        mqttConfig.setPort(mqtt.getPort());
        mqttConfig.setTopicPrefix(mqtt.getTopicPrefix());
        MqttConfig.HaDiscoveryConfig haDiscoveryConfig = new MqttConfig.HaDiscoveryConfig();
        haDiscoveryConfig.setEnabled(mqtt.getHaDiscovery().isEnabled());
        haDiscoveryConfig.setTopicPrefix(mqtt.getHaDiscovery().getTopicPrefix());
        mqttConfig.setHaDiscovery(haDiscoveryConfig);
        return mqttConfig;
    }

}
