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
import com.dfi.sbc2ha.config.boneio.definition.cover.BoneioCoverConfig;
import com.dfi.sbc2ha.config.boneio.definition.filter.BoneIoValueFilterType;
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
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.LoggerConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.ActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.CoverActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.MqttActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.OutputActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.BinarySensorDeviceClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SwitchDeviceClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.extentsionBoard.ExtensionBoardsConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.OledConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.PlatformConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.DallasBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.I2cBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.Lm75BusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.ModbusBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.BusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.AnalogSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class BoneIoConverter {

    private static boolean mcpInverted = false;

    static AppConfig convertBoneIo(BoneIoConfig boneIoConfig, String inputBoard, String outputBoard) {
        return convert(boneIoConfig,
                "boneio",
                inputBoard == null ? detectInputBoard(boneIoConfig.getInput()) : inputBoard,
                outputBoard == null ? detectOutputBoard(boneIoConfig.getOutput()) : outputBoard
        );
    }

    static AppConfig convertRpi(BoneIoConfig boneIoConfig, String inputBoard, String outputBoard) {
        return convert(boneIoConfig,
                "rpi",
                inputBoard == null ? "nohat" : inputBoard,
                outputBoard == null ? "nohat" : outputBoard);

    }

    static AppConfig convert(BoneIoConfig boneIoConfig, String vendor, String inputBoard, String outputBoard) {
        log.warn("running conversion with vendor: {}, inputBoard: {}, outputBoard: {}", vendor, inputBoard, outputBoard);


        AppConfig appConfig = new AppConfig();
        appConfig.setExtensionBoards(getExtensionBoardsConfig(vendor, inputBoard, outputBoard));

        appConfig.addPlatform(getMqttConfig(boneIoConfig.getMqtt()));
        appConfig.addPlatform(getOledConfig(boneIoConfig.getOled()));

        appConfig.addPlatforms(getBusConfig(boneIoConfig.getMcp23017(), PlatformType.MCP23017));
        appConfig.addPlatforms(getBusConfig(boneIoConfig.getPca9685(), PlatformType.PCA9685));
        appConfig.addPlatforms(getBusConfig(boneIoConfig.getDs2482(), PlatformType.DS2482));

        appConfig.addPlatform(getDallas(boneIoConfig.getDallas()));
        appConfig.addPlatform(getModbus(boneIoConfig.getModbus()));

        appConfig.setActuator(getOutput(boneIoConfig.getOutput()));
        appConfig.addActuator(getCover(boneIoConfig.getCover()));

        appConfig.addSensors(getLm75Config(boneIoConfig.getLm75(), appConfig));
        appConfig.addSensors(getModbusSensors(boneIoConfig.getModbusSensors()));
        appConfig.addSensors(getSensor(boneIoConfig.getSensor()));
        appConfig.addSensors(getAdc(boneIoConfig.getAdc()));

        appConfig.addSensors(getInput(boneIoConfig.getInput(), appConfig.getActuator()));
        appConfig.setLogger(getLogger(boneIoConfig.getLogger()));

        return appConfig;
    }


    private static ExtensionBoardsConfig getExtensionBoardsConfig(String vendor, String inputB, String outputB) {


        ExtensionBoardsConfig ebc = new ExtensionBoardsConfig();
        ebc.setVendor(vendor);
        ebc.setInputBoard(inputB);
        ebc.setOutputBoard(outputB);

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

        log.warn("guessing inputBoard: {}", inputBoard);

        return inputBoard;
    }

    private static String detectOutputBoard(List<BoneIoOutputConfig> outputList) {
        String outputBoard = "output-24x16A-v0.4";
        if (outputList.size() > 24) {
            outputBoard = "output-32x5A-v0.4";
        }

        log.warn("guessing outputBoard: {}", outputBoard);

        return outputBoard;
    }


    static List<PlatformConfig> getBusConfig(List<BoneIoBusConfig> l, PlatformType pt) {
        List<PlatformConfig> configList = l.stream().map(s -> {
            I2cBusConfig d = new I2cBusConfig();
            d.setBusId(s.getId());
            d.setAddress(s.getAddress());
            d.setPlatform(pt);
            return d;
        }).collect(Collectors.toList());

        if (configList.size() == 2 && ((I2cBusConfig) configList.get(0)).getAddress() > ((I2cBusConfig) configList.get(1)).getAddress()) {
            I2cBusConfig first = (I2cBusConfig) configList.get(0);
            I2cBusConfig second = (I2cBusConfig) configList.get(1);
            var tmp = first.getAddress();
            first.setAddress(second.getAddress());
            second.setAddress(tmp);
            mcpInverted = true;
        }

        return configList;
    }

    static LoggerConfig getLogger(BoneIoLoggerConfig s) {
        LoggerConfig d = new LoggerConfig();
        d.setDefaultLevel(s.getDefaultLevel());
        Map<String, String> logs = new LinkedHashMap<>(s.getLogs());
        d.setLogs(logs);
        return d;
    }

    static List<SensorConfig> getAdc(List<BoneIoAdcSensorConfig> adc) {
        List<SensorConfig> l = new ArrayList<>();
        for (var s : adc) {
            AnalogSensorConfig d = new AnalogSensorConfig();
            d.setName(s.getId());
            d.setAnalog(detectAnalogInput(s.getPin()));

            d.setUpdateInterval(s.getUpdateInterval());
            d.setFilters(getFilters(s.getFilters()));
            l.add(d);
        }
        return l;
    }

    static List<SensorConfig> getInput(List<BoneIoInputConfig<?>> sl, List<ActuatorConfig> actuator) {
        return sl.stream().map(s -> {
            switch (s.getKind()) {
                default:
                case SWITCH:
                    return getInputSwitchConfig((BoneIoInputSwitchConfig) s, actuator);
                case SENSOR:
                    return getInputSensorConfig((BoneIoInputSensorConfig) s, actuator);
            }
        }).collect(Collectors.toList());
    }

    static InputSensorConfig getInputSensorConfig(BoneIoInputSensorConfig s, List<ActuatorConfig> actuator) {
        var d = new InputSensorConfig();
        d.setDeviceClass(convertEnum(BinarySensorDeviceClassType.class, s.getDeviceClass()));
        setInputConfig(s, d);

        var sa = s.getActions();
        Map<InputSensorAction, List<ActionConfig>> da = new LinkedHashMap<>();
        sa.forEach((san, sc) -> da.put(convertEnum(InputSensorAction.class, san), getActions(sc, actuator)));
        d.setActions(da);

        return d;
    }

    static InputSwitchConfig getInputSwitchConfig(BoneIoInputSwitchConfig s, List<ActuatorConfig> actuator) {
        var d = new InputSwitchConfig();
        d.setDeviceClass(convertEnum(SwitchDeviceClassType.class, s.getDeviceClass()));
        d.setClickDetection(ButtonState.LONG);
        setInputConfig(s, d);

        var sa = s.getActions();
        Map<InputSwitchAction, List<ActionConfig>> da = new LinkedHashMap<>();
        sa.forEach((san, sc) -> da.put(convertEnum(InputSwitchAction.class, san), getActions(sc, actuator)));
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

    static void setInputConfig(BoneIoInputConfig<?> s, InputConfig d) {
        d.setName(s.getId());
        d.setInput(detectDigitalInput(s.getPin()));

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


    static List<ActionConfig> getActions(List<BoneIoActionConfig> sla, List<ActuatorConfig> actuator) {
        List<ActionConfig> dla = new ArrayList<>();
        for (var sa : sla) {
            ActionConfig da;
            switch (sa.getAction()) {
                case MQTT:
                    da = getMqttAction((BoneIoMqttActionConfig) sa);
                    break;
                default:
                case OUTPUT:
                    da = getOutputAction((BoneIoOutputActionConfig) sa, actuator);
                    break;
                case COVER:
                    da = getCoverAction((BoneIoCoverActionConfig) sa, actuator);
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

    static OutputActionConfig getOutputAction(BoneIoOutputActionConfig s, List<ActuatorConfig> list) {
        ActuatorConfig a = list.stream().filter(c -> c.getName().equals(s.getPin())).findFirst().orElseThrow();
        OutputActionConfig d = new OutputActionConfig();
        d.setOutput(a.getOutput());
        d.setActionOutput(convertEnum(ActionOutputType.class, s.getActionOutput()));
        return d;

    }

    static CoverActionConfig getCoverAction(BoneIoCoverActionConfig s, List<ActuatorConfig> list) {
        ActuatorConfig a = list.stream().filter(c -> c.getName().equals(s.getPin())).findFirst().orElseThrow();
        CoverActionConfig d = new CoverActionConfig();
        d.setOutput(a.getOutput());
        d.setActionCover(convertEnum(ActionCoverType.class, s.getActionCover()));
        return d;
    }

    static List<ActuatorConfig> getOutput(List<BoneIoOutputConfig> sl) {
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

    private static List<ActuatorConfig> getCover(List<BoneioCoverConfig> sl) {
        return sl.stream().map(BoneIoConverter::getCoverConfig).collect(Collectors.toList());
    }

    private static ActuatorConfig getCoverConfig(BoneioCoverConfig s) {
        CoverConfig d = new CoverConfig();
        d.setOutputType(ActuatorType.COVER);
        d.setKind(OutputKindType.COVER);
        d.setName(s.getId());
        d.setOpenRelay(s.getOpenRelay());
        d.setCloseRelay(s.getCloseRelay());
        d.setOpenTime(s.getOpenTime());
        d.setCloseTime(s.getCloseTime());
        d.setDeviceClass(s.getDeviceClass());
        d.setShowInHa(s.isShowInHa());
        d.setRestoreState(s.isRestoreState());
        return d;
    }

    static void getOutput(BoneIoOutputConfig s, ActuatorConfig d) {
        d.setName(s.getId());
        d.setOutput(detectOutput(s));
        d.setOutputType(convertEnum(ActuatorType.class, s.getOutputType()));
        d.setRestoreState(s.isRestoreState());
        d.setMomentaryTurnOff(s.getMomentaryTurnOff());
        d.setMomentaryTurnOn(s.getMomentaryTurnOn());
    }

    private static int detectOutput(BoneIoOutputConfig s) {
        ExtensionBoardInfo instance = ExtensionBoardInfo.getInstance();

        if (s instanceof BoneIoMcpOutputConfig) {
            int mcpId = Integer.parseInt(((BoneIoMcpOutputConfig) s).getMcpId().replace("mcp", ""));
            if (mcpInverted) {
                mcpId = mcpId == 1 ? 2 : 1;
            }
            int pinId = Integer.parseInt(s.getPin());
            return instance.getOutputMcpByPin(pinId, mcpId);
        } else {
            return instance.getOutputRpiByGpio(Integer.parseInt(s.getPin()));
        }
    }

    static PcaOutputConfig getPcaOutput(BoneIoPcaOutputConfig s) {
        var d = new PcaOutputConfig();
        getOutput(s, d);
        //d.setPcaId(s.getPcaId());
        d.setPercentageDefaultBrightness(s.getPercentageDefaultBrightnes());
        return d;
    }

    static McpOutputConfig getMcpOutput(BoneIoMcpOutputConfig s) {
        var d = new McpOutputConfig();
        getOutput(s, d);

        return d;
    }

    static GpioOutputConfig getGpioOutput(BoneIoGpioOutputConfig s) {
        var d = new GpioOutputConfig();
        getOutput(s, d);

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
        d.setName(s.getId());
        d.setBusId(s.getBusId());
        d.setUpdateInterval(s.getUpdateInterval());
        d.setFilters(getFilters(s.getFilters()));
        d.setShowInHa(s.isShowInHa());
    }

    static List<SensorConfig> getModbusSensors(List<BoneIoModbusSensorConfig> sl) {
        return sl.stream().map(s -> {
            var d = new ModbusSensorConfig();
            d.setName(s.getId());
            d.setAddress(s.getAddress());
            d.setModel(s.getModel());
            d.setUpdateInterval(s.getUpdateInterval());

            return d;
        }).collect(Collectors.toList());
    }

    static ModbusBusConfig getModbus(BoneIoModbusBusConfig s) {
        if (s == null) return null;
        var d = new ModbusBusConfig();
        d.setBusId(s.getId());
        d.setUart(UartType.valueOf(s.getUart().name()));
        return d;
    }

    static DallasBusConfig getDallas(BoneIoFsBusConfig s) {
        if (s == null) return null;
        var d = new DallasBusConfig();
        d.setBusId(s.getId());
        return d;
    }

    static List<SensorConfig> getLm75Config(List<BoneIoLm75SensorConfig> lm75, AppConfig appConfig) {
        List<SensorConfig> list = new ArrayList<>();
        for (var s : lm75) {
            Lm75SensorConfig d = new Lm75SensorConfig();
            d.setName(s.getId());
            d.setUnitOfMeasurement(s.getUnitOfMeasurement());
            d.setUpdateInterval(s.getUpdateInterval());
            d.setFilters(getFilters(s.getFilters()));

            Lm75BusConfig platformConfig = appConfig.getPlatform(PlatformType.LM75, Lm75BusConfig.class).stream()
                    .filter(pc -> ((I2cBusConfig) pc).getAddress() == s.getAddress())
                    .findFirst()
                    .orElseGet(() -> createLm75Platfrom(appConfig, s));

            d.setBusId(platformConfig.getBusId());

            appConfig.getSensor().add(d);
        }
        return list;
    }

    private static Lm75BusConfig createLm75Platfrom(AppConfig appConfig, BoneIoLm75SensorConfig s) {
        Lm75BusConfig pc = new Lm75BusConfig();
        pc.setAddress(s.getAddress());
        pc.setBusId("Lm75");
        appConfig.addPlatform(pc);

        return pc;
    }

    static List<Map<ValueFilterType, Number>> getFilters(List<Map<BoneIoValueFilterType, Number>> filterList) {
        return filterList.stream().map(filter -> {
            Map<ValueFilterType, Number> dict = new HashMap<>();
            filter.forEach((s, s2) -> {
                dict.put(ValueFilterType.valueOf(s.name()), s2);
            });
            return dict;
        }).collect(Collectors.toList());

    }

    static OledConfig getOledConfig(BoneIoOledConfig oled) {
        if (oled == null) return null;
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
