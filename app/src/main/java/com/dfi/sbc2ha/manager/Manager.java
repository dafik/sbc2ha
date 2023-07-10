package com.dfi.sbc2ha.manager;

import com.dfi.sbc2ha.Easing;
import com.dfi.sbc2ha.Version;
import com.dfi.sbc2ha.actuator.*;
import com.dfi.sbc2ha.bus.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.ActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.CoverActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.MqttActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.OutputActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionOutputType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.AdcSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import com.dfi.sbc2ha.display.Display;
import com.dfi.sbc2ha.display.DisplayFactory;
import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.helper.StateManager;
import com.dfi.sbc2ha.helper.bus.BusFactory;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionOutputBoard;
import com.dfi.sbc2ha.helper.ha.DeviceState;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.message.Availability;
import com.dfi.sbc2ha.helper.ha.autodiscovery.message.DeviceTriggerAvailability;
import com.dfi.sbc2ha.helper.ha.autodiscovery.message.LedAvailability;
import com.dfi.sbc2ha.helper.ha.command.LightCommand;
import com.dfi.sbc2ha.helper.mqtt.MqttConfigHelper;
import com.dfi.sbc2ha.helper.mqtt.MqttHelper;
import com.dfi.sbc2ha.helper.mqtt.hivemq.MqttHelperHiveRx;
import com.dfi.sbc2ha.helper.mqtt.paho.MqttListener;
import com.dfi.sbc2ha.helper.stats.StatsProvider;
import com.dfi.sbc2ha.modbus.Modbus;
import com.dfi.sbc2ha.sensor.Sensor;
import com.dfi.sbc2ha.sensor.SensorFactory;
import com.dfi.sbc2ha.sensor.StateEvent;
import com.dfi.sbc2ha.sensor.analog.AnalogSensor;
import com.dfi.sbc2ha.sensor.binary.Binary;
import com.dfi.sbc2ha.sensor.binary.BinarySensor;
import com.dfi.sbc2ha.sensor.binary.Button;
import com.dfi.sbc2ha.sensor.binary.ButtonState;
import com.dfi.sbc2ha.sensor.modbus.ModbusSensor;
import com.dfi.sbc2ha.sensor.modbus.ModbusSensorFactory;
import com.dfi.sbc2ha.sensor.temperature.Lm75TempSensor;
import com.dfi.sbc2ha.sensor.temperature.oneWire.W1TempSensor;
import com.diozero.api.I2CDevice;
import com.diozero.devices.oneWire.bus.DS248.DS2482Bus;
import com.diozero.devices.oneWire.bus.fs.FsBus;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.tinylog.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.dfi.sbc2ha.helper.ha.autodiscovery.message.Availability.*;
import static com.dfi.sbc2ha.helper.mqtt.hivemq.MqttHelperHiveRx.payloadToString;

public class Manager implements AutoCloseable, Consumer<ManagerCommand>, MqttListener {

    private final Map<String, Sensor<?, ?, ?>> sensorMap = new HashMap<>();
    private final Map<String, Actuator<?, ?, ?>> actuatorMap = new HashMap<>();
    private final List<Bus<?>> busMap = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StateManager stateManager;
    private final ManagerExecutor commandExecutor;
    private MqttConfigHelper mqttConfigHelper;

    private MqttHelper mqttHelper;
    private Display display;
    private StatsProvider statsProvider;


    public Manager(AppConfig config) {


        setupBoard(config);
        stateManager = StateManager.getInstance();
        commandExecutor = new ManagerExecutor(this);

        setupDisplay(config.getPlatform(PlatformType.OLED, OledConfig.class), config.getActuatorLabels());

        //displayTest();

        setupMqtt(config.getPlatform(PlatformType.MQTT, MqttConfig.class));
        subscribeHaCommands();
        subscribeHaDiscovery();

        prepareAutodiscovery(config.getPlatform(PlatformType.MQTT, MqttConfig.class));

        waitForMqttReadyForPublish();
        sendStatus(DeviceState.ONLINE);

        registerModbus(config.getPlatform(PlatformType.MODBUS, ModbusBusConfig.class));
        registerModbusSensor(config.getSensor(PlatformType.MODBUS, ModbusSensorConfig.class));

        addDisplayLine("prepare buses");

        registerMcpBus(config.getPlatform(PlatformType.MCP23017, I2cBusConfig.class));
        registerOneWireDS2484Bus(config.getPlatform(PlatformType.DS2482, I2cBusConfig.class));
        registerOneWireFsBus(config.getPlatform(PlatformType.DALLAS, DallasBusConfig.class));
        registerLM75Bus(config.getPlatform(PlatformType.LM75, I2cBusConfig.class));

        addDisplayLine("prepare actuators");
        registerActuators(config.getActuator());

        addDisplayLine("prepare sensors");

        registerAdc(config.getSensor(PlatformType.ANALOG, AdcSensorConfig.class));
        registerDS18B20(config.getSensor(PlatformType.DS2482, DS18B20.class));
        registerDS18B20(config.getSensor(PlatformType.DALLAS, DS18B20.class));
        registerTempLM75(config.getSensor(PlatformType.LM75, Lm75SensorConfig.class));

        registerSensors(config.getSensor(PlatformType.GPIO, InputConfig.class));

        cleanAutodiscovery();
        sendStatus(DeviceState.ONLINE);

        addDisplayLine("running stats provider");
        if (statsProvider != null) {
            Scheduler.getInstance().submit(statsProvider);
            if (null != display) {
                display.setStarted();

                addDisplayLine("ready");
                display.handleClick(null);
            }
        }


        //- removing: homeassistant/button/boneio-1/restart/config
        //- removing: homeassistant/button/boneio-1/logger/config


        Diozero.registerForShutdown(this);
    }


    private static void handleRelayAction(Relay actuator, ActionOutputType actionOutputType, String command) {
        switch (actionOutputType) {
            case TOGGLE:
                actuator.toggle();
                break;
            case ON:
                actuator.turnOn();
                break;
            case OFF:
                actuator.turnOff();
                break;
        }
    }

    private void handleLedAction(Led actuator, String action, String command) {
        LightCommand lightCommand;
        try {
            lightCommand = objectMapper.readValue(action, LightCommand.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (lightCommand.getTransition() > 0 && lightCommand.getEffect() == null) {
            lightCommand.setEffect(Easing.linerIn);
        }

        if (lightCommand.getBrightness() > 0) {
            actuator.setBrightness(LedAvailability.convertFromHa(lightCommand.getBrightness()));
            return;
        }
        ActionOutputType actionOutputType = ActionOutputType.valueOf(lightCommand.getState());
        switch (actionOutputType) {
            case TOGGLE:
                actuator.toggle(lightCommand);
                break;
            case ON:
                actuator.turnOn(lightCommand);
                break;
            case OFF:
                actuator.turnOff(lightCommand);
                break;
        }
    }

    public void sendStatus(DeviceState deviceState, String device) {
        Logger.debug("Sending {} state device: {}", deviceState, device);
        String topic = formatTopic(mqttConfigHelper.getTopicPrefix(), device + Constants.STATE);
        mqttHelper.publish(topic, deviceState.getLabel().getBytes(), true);
    }

    public void sendStatus(DeviceState deviceState) {
        Logger.debug("Sending {} state", deviceState);
        String topic = formatTopic(mqttConfigHelper.getTopicPrefix(), Constants.STATE);


        mqttHelper.publish(topic, deviceState.getLabel().getBytes(), true);
    }

    private void addDisplayLine(String line) {
        Logger.debug("display add: {}", line);
        if (display != null) {
            display.addBootLine(line);
        }
    }

    private void setupDisplay(List<OledConfig> configList, List<String> outputIds) {
        for (var config : configList) {
            if (config.isEnabled()) {
                statsProvider = new StatsProvider(stateManager, outputIds);
                display = DisplayFactory.createDisplay(config, statsProvider);
            }
        }
    }

    private void waitForMqttReadyForPublish() {
        addDisplayLine("wait for mqtt ready");
        while (!mqttHelper.isReadyForPublish()) {

            try {
                Logger.debug("Wait for mqtt ready");
                //noinspection BusyWait
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void cleanAutodiscovery() {
        addDisplayLine("clear autodiscovery");
        Set<String> dm = mqttConfigHelper.getAutodiscoveryMessages().keySet();
        for (String v : mqttConfigHelper.getDiscoveredMessages().keySet()) {
            if (!dm.contains(v)) {
                Logger.debug("removing: {}", v);
                mqttHelper.publish(v, null, true);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void subscribeHaCommands() {
        addDisplayLine("subscribe ha events");
        mqttHelper.createSubscription(mqttConfigHelper.getCommandTopic(), this::handleHaCommands);
    }

    private void subscribeHaDiscovery() {
        addDisplayLine("subscribe ha discovery");
        mqttHelper.createSubscription(mqttConfigHelper.getHaDiscoveryTopic(), this::handleHaDiscovery);
    }

    private void handleHaCommands(Mqtt5Publish publish) {
        Logger.trace("Received publish topic:  {}, QoS: {}, payload: {}",
                publish.getTopic(), publish.getQos(), payloadToString(publish.getPayloadAsBytes()));
        MqttTopic topic = publish.getTopic();
        if (topic.toString().contains(CMD)) {
            commandExecutor.addExternalCommand(new ManagerCommandExternal(publish.getTopic().toString(), publish.getPayloadAsBytes()));
        }
    }

    private void handleHaDiscovery(Mqtt5Publish publish) {
        Logger.trace("Received publish topic:  {}, QoS: {}, payload: {}",
                publish.getTopic(), publish.getQos(), payloadToString(publish.getPayloadAsBytes()));
        mqttConfigHelper.addDiscoveredMessages(publish.getTopic().toString(), payloadToString(publish.getPayloadAsBytes()));
    }

    private void prepareAutodiscovery(List<MqttConfig> configList) {
        if (configList.size() == 0) {
            return;
        }
        var mqtt = configList.get(0);
        if (mqtt.getHaDiscovery().isEnabled()) {
            addDisplayLine("prepare autodiscovery");
            Availability.setTopicPrefix(mqtt.getTopicPrefix());
            Availability.setModel("boneIO Relay Board");
            Availability.setVersion(Version.VERSION);
        }
    }

    private void setupMqtt(List<MqttConfig> configList) {
        if (configList.size() == 0) {
            return;
        }
        var mqtt = configList.get(0);
        Logger.debug("setup mqtt");
        addDisplayLine("setup mqtt");
        mqttConfigHelper = new MqttConfigHelper(mqtt);

        mqttHelper = new MqttHelperHiveRx(mqtt, mqttConfigHelper);

        addDisplayLine("start mqtt");
        mqttHelper.start();

    }

    private void setupBoard(AppConfig config) {

        Logger.debug("Setup board");
        addDisplayLine("setup board");
        try {
            NativeDeviceFactoryInterface ndf = DeviceFactoryHelper.getNativeDeviceFactory();
            BoardInfo boardInfo = ndf.getBoardInfo();

            if (config.getExtensionBoards() != null) {
                ExtensionBoardInfo.initialize(config.getExtensionBoards().getVendor(), config.getExtensionBoards().getInputBoard(), config.getExtensionBoards().getOutputBoard());
            }


            Logger.info("Found SBC name: {}  mem: {}", boardInfo.getLongName(), boardInfo.getMemoryKb());
        } catch (RuntimeException e) {
            Logger.error(e);
        }
    }

    private <T extends Bus<?>> List<T> getBus(PlatformType type, Class<T> listType) {
        return busMap.stream()
                .filter(bus -> bus.getPlatformType() == type)
                .map(listType::cast)
                .collect(Collectors.toList());
    }

    private <T extends Bus<?>> T getBus(PlatformType type, Class<T> listType, String id) {
        return busMap.stream()
                .filter(bus -> bus.getPlatformType() == type)
                .filter(bus -> bus.getId().equals(id))
                .map(listType::cast)
                .findFirst()
                .orElseThrow();
    }

    private void registerTempLM75(List<Lm75SensorConfig> configList) {
        if (configList.size() > 0) {
            addDisplayLine("prepare LM75");
        }
        for (var config : configList) {
            try {
                Lm75Bus bus = getBus(PlatformType.LM75, config.getBusId(), Lm75Bus.class);

                Lm75TempSensor temp = SensorFactory.createLM75Temp(config, bus);
                sensorMap.put(temp.getName(), temp);
                float initialValue = temp.getTemperature();

                Availability availability = registerHaSensorActions(config, temp);

                sendInitialState(initialValue, availability);


            } catch (RuntimeException logged) {
                Logger.error("unable setup lm75: {} :{}", config.getId(), logged.getMessage());
            }

        }
    }

    private void registerModbusSensor(List<ModbusSensorConfig> configList) {
        if (configList.size() > 0) {
            addDisplayLine("prepare Modbus device");
        }
        for (ModbusSensorConfig config : configList) {
            try {
                addDisplayLine("prepare Modbus device" + config.getId());

                ModbusBus bus = getBus(PlatformType.MODBUS, "modbus", ModbusBus.class);

                ModbusSensor sensor = ModbusSensorFactory.createDevice(config, bus);

                if (sensor.isAvailable()) {
                    sensorMap.put(sensor.getName(), sensor);
                    registerHaSensorActions(sensor, config.getModel());

                    sendStatus(DeviceState.ONLINE, sensor.getId());
                }


            } catch (RuntimeException e) {
                Logger.error("unable register modbus: {}", e.getMessage());
            }

        }
    }

    private void registerDS18B20(List<DS18B20> configList) {
        if (configList.size() > 0) {
            addDisplayLine("prepare DS18B20");
        }
        for (var sensorConfig : configList) {
            try {
                OWireBus bus;
                switch (sensorConfig.getPlatform()) {
                    case DALLAS:
                        bus = getBus(PlatformType.DALLAS, sensorConfig.getBusId(), OWireBus.class);
                        break;
                    case DS2482:
                        bus = getBus(PlatformType.DS2482, sensorConfig.getBusId(), OWireBus.class);
                        break;
                    default:
                        throw new IllegalArgumentException("bad platform type for ds18b20");
                }


                W1TempSensor input = SensorFactory.createOneWireTherm(sensorConfig, bus);
                sensorMap.put(input.getName(), input);
                float initialValue = input.getTemperature();

                Availability availability = registerHaSensorActions(sensorConfig, input);
                sendInitialState(initialValue, availability);

            } catch (RuntimeException e) {
                Logger.error("failed create 1-wire sensor id: {} cause: {}", sensorConfig.getId(), e.getMessage());
            }
        }
    }

    private void registerSensors(List<InputConfig> configList) {
        if (configList.size() > 0) {
            addDisplayLine("prepare inputs");
        }
        for (var config : configList) {
            try {
                BinarySensor<? extends StateEvent<?>, ?> input = SensorFactory.createBinarySensor(config);
                sensorMap.put(input.getName(), input);

                if (config instanceof InputSwitchConfig) {
                    registerHaDeviceTriggers((InputSwitchConfig) config, input);
                    registerInternalActions((InputSwitchConfig) config, (Button) input);
                } else {
                    registerHaSensorActions(config, input);
                    registerInternalActions((InputSensorConfig) config, (Binary) input);
                }

            } catch (RuntimeException logged) {
                Logger.error("unable setup sensor: {} :{}", config.getId(), logged.getMessage());
            }
        }
    }

    private void registerAdc(List<AdcSensorConfig> configList) {
        if (configList.size() > 0) {
            addDisplayLine("prepare ADC");
        }
        for (var config : configList) {
            try {
                AnalogSensor input = SensorFactory.createAnalogSensor(config);
                sensorMap.put(input.getName(), input);

                float initialValue = input.getValue();

                Availability availability = registerHaSensorActions(config, input);
                sendInitialAdcState(initialValue, availability);

            } catch (RuntimeException logged) {
                Logger.error("unable setup adc: {} :{}", config.getId(), logged.getMessage());
            }
        }
    }

    private void registerActuators(List<ActuatorConfig> configList) {
        for (ActuatorConfig config : configList) {
            try {
                Number initialValue = stateManager.get(SbcDeviceType.RELAY.toString(), config.getName());
                ExtensionOutputBoard.OutputDefinition outputDefinition = ExtensionBoardInfo.getInstance().getOut().get(config.getOutput());
                Bus<?> bus;
                switch (config.getKind()) {
                    default:
                    case GPIO:
                        bus = null;
                        break;
                    case MCP:
                        bus = getBus(PlatformType.MCP23017, "mcp" + outputDefinition.getId(), MCP23017Bus.class);
                        break;
                    case PCA:
                        bus = getBus(PlatformType.PCA9685, String.valueOf(outputDefinition.getId()), PCA9685Bus.class);
                        break;
                }
                Actuator<?, ?, ?> actuator = ActuatorFactory.getActuator(config, bus, initialValue.floatValue());
                Availability availability;
                if (actuator instanceof Led) {
                    availability = registerHaActuatorActions(config, (Led) actuator);
                    registerStateManager((Led) actuator);
                    LedEvent evt = new LedEvent(System.currentTimeMillis(), initialValue.floatValue(),
                            initialValue.floatValue() > 0 ? ActuatorState.ON : ActuatorState.OFF);
                    sendLedEvent(evt, (LedAvailability) availability);
                } else {
                    availability = registerHaActuatorActions(config, (Relay) actuator);
                    registerStateManager((Relay) actuator);
                    sendInitialState(initialValue.intValue() > 0, availability);
                }


                actuatorMap.put(String.valueOf(config.getOutput()), actuator);
            } catch (RuntimeException logged) {
                Logger.error("unable setup actuator: {} :{}", config.getId(), logged.getMessage());
            }
        }
    }

    private void registerStateManager(Relay actuator) {
        actuator.addListenerAny(actuatorEvent -> stateManager.handlerState(actuator, actuatorEvent));
    }

    private void registerStateManager(Led actuator) {
        actuator.addListenerAny(actuatorEvent -> stateManager.handlerState(actuator, actuatorEvent));
    }

    private void sendInitialState(boolean initialValue, Availability availability) {
        ActuatorState state = initialValue ? ActuatorState.ON : ActuatorState.OFF;

        String payload = "{\"state\":\"" + state + "\"}";
        mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
    }

    private void sendInitialState(Float initialValue, Availability availability) {
        String payload = "{\"state\":\"" + initialValue + "\"}";
        mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
    }

    private void sendLedEvent(LedEvent evt, LedAvailability availability) {
        if (evt.getBrightness() == 0 && evt.getState() == ActuatorState.ON) {
            return;
        }
        //Logger.debug("sending led state: {}", evt);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(evt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //String payload = "{\"state\":\"" + evt.getState() + "\",\"brightness\":" + evt.getBrightness() + "}";
        mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
    }

    private void sendInitialAdcState(Float initialValue, Availability availability) {
        String payload = initialValue.toString();
        mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
    }

    private void registerMcpBus(List<I2cBusConfig> mcpBusList) {
        if (mcpBusList.size() > 0) {
            addDisplayLine("prepare mcp");
        }
        for (I2cBusConfig busConfig : mcpBusList) {
            try {
                MCP23017Bus bus = BusFactory.createMcp(busConfig);
                busMap.add(bus);

            } catch (RuntimeException logged) {
                Logger.error("unable setup mcp: {} :{}", busConfig.getId(), logged.getMessage());
            }
        }
    }

    private void registerLM75Bus(List<I2cBusConfig> i2cBusList) {
        if (i2cBusList.size() > 0) {
            addDisplayLine("prepare mcp");
        }
        for (I2cBusConfig busConfig : i2cBusList) {
            try {
                I2CDevice bus = BusFactory.createI2c(busConfig);
                busMap.add(new Lm75Bus(bus, busConfig.getId()));

            } catch (RuntimeException logged) {
                Logger.error("unable setup mcp: {} :{}", busConfig.getId(), logged.getMessage());
            }
        }
    }

    private void registerOneWireDS2484Bus(List<I2cBusConfig> ds2482BusList) {
        if (ds2482BusList.size() > 0) {
            addDisplayLine("prepare ds2482");
        }
        for (I2cBusConfig busConfig : ds2482BusList) {
            try {
                DS2482Bus bus = BusFactory.createOneWireDS2482(busConfig);
                busMap.add(new OWireBus(PlatformType.DS2482, bus, busConfig.getId()));

            } catch (RuntimeException logged) {
                Logger.error("unable setup Ds2482 1-wire: {} :{}", busConfig.getId(), logged.getMessage());
            }
        }
    }

    private void registerOneWireFsBus(List<DallasBusConfig> configList) {
        if (configList.size() > 0) {
            addDisplayLine("prepare 1-wire FS");
        }
        for (var busConfig : configList) {
            try {
                FsBus bus = BusFactory.createOneWireFs(busConfig);
                busMap.add(new OWireBus(PlatformType.DS2482, bus, busConfig.getId()));
            } catch (RuntimeException logged) {
                Logger.error("unable setup fs 1-wire: {} :{}", busConfig.getId(), logged.getMessage());
            }
        }
    }

    private void registerModbus(List<ModbusBusConfig> configList) {
        addDisplayLine("prepare Modbus");
        for (var config : configList) {
            try {
                Modbus bus = BusFactory.createModbus(config);
                busMap.add(new ModbusBus(bus, config.getId()));
            } catch (RuntimeException logged) {
                Logger.error("unable setup modbus: {} :{}", config.getId(), logged.getMessage());
            }
        }
    }

    private Availability registerHaActuatorActions(ActuatorConfig config, Relay actuator) {
        Availability availability = Availability.getActuatorAvailability(config);
        if (availability != null) {
            sendHaAutodiscovery(availability);
            actuator.whenAny(event -> {
                String payload = "{\"state\":\"" + event.getState().name() + "\"}";
                mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
            });

        }
        return availability;
    }

    private Availability registerHaActuatorActions(ActuatorConfig config, Led actuator) {
        LedAvailability availability = (LedAvailability) Availability.getActuatorAvailability(config);
        if (availability != null) {
            sendHaAutodiscovery(availability);
            actuator.whenAny(event -> sendLedEvent(event, availability));
        }
        return availability;
    }

    private void registerHaSensorActions(ModbusSensor device, String model) {
        ModbusSensorDefinition def = device.getDefinition();


        for (var base : def.getRegistersBase()) {
            for (var register : base.getRegisters()) {
                Availability availability = Availability.getModbusSensorAvailability(register, device, model, base.getBase());
                sendHaAutodiscovery(availability);
                device.whenAnyRegister(event -> {
                    String payload = "{\"state\":\"" + event.getValue() + "\"}";
                    mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
                }, register);
            }
        }
        device.whenState(state -> sendStatus(state, device.getId()));


    }

    private Availability registerHaSensorActions(Lm75SensorConfig config, Lm75TempSensor input) {
        Availability availability = Availability.getSensorAvailability(config);

        sendHaAutodiscovery(availability);
        input.whenAny(event -> {
            String payload = "{\"state\":\"" + event.getValue() + "\"}";
            mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
        });

        return availability;
    }

    private Availability registerHaSensorActions(SensorConfig config, W1TempSensor input) {
        Availability availability = Availability.getSensorAvailability(config);

        sendHaAutodiscovery(availability);
        input.whenAny(event -> {
            String payload = "{\"state\":\"" + event.getValue() + "\"}";
            mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
        });

        return availability;
    }

    private Availability registerHaSensorActions(AdcSensorConfig config, AnalogSensor input) {
        Availability availability = Availability.getSensorAvailability(config);

        sendHaAutodiscovery(availability);
        input.whenAny(event -> {
            String payload = String.valueOf(event.getValue());
            mqttHelper.publish(availability.getStateTopic(), payload.getBytes(), false);
        });

        return availability;
    }

    private void registerHaSensorActions(InputConfig<?> config, BinarySensor<? extends StateEvent<?>, ?> input) {
        Availability availability = Availability.getSensorAvailability(config);
        if (config.isShowInHa()) {
            sendHaAutodiscovery(availability);
            input.whenAny(event -> {
                byte[] payload;
       /*         if (event.getState() == ButtonState.RELEASE) {
                    payload = new byte[0];
                } else {*/
                payload = event.getState().toString().toLowerCase().getBytes();
                //}
                mqttHelper.publish(availability.getStateTopic(), payload, false);
            });
        }
    }

    private void registerHaDeviceTriggers(InputSwitchConfig config, BinarySensor<? extends StateEvent<?>, ?> input) {
        if (config.isShowInHa()) {
            DeviceTriggerAvailability availability;
            switch (config.getClickDetection()) {
                default:
                case SINGLE:
                    availability = Availability.getDeviceTriggerAvailability(config,
                            List.of(ButtonState.SINGLE));
                    break;
                case DOUBLE:
                    availability = Availability.getDeviceTriggerAvailability(config,
                            List.of(ButtonState.SINGLE, ButtonState.DOUBLE));
                    break;
                case LONG:
                    availability = Availability.getDeviceTriggerAvailability(config,
                            List.of(ButtonState.SINGLE, ButtonState.DOUBLE, ButtonState.LONG, ButtonState.RELEASE));
                    break;
            }
            for (var avail : availability) {
                sendHaAutodiscovery(avail);
            }
            input.whenAny(event -> {
                byte[] payload = event.getState().toString().toLowerCase().getBytes();
                mqttHelper.publish(availability.getTopic(), payload, false);
            });
        }
    }

    private void registerInternalActions(InputSwitchConfig config, Button input) {
        if (config.getActions().isEmpty()) {
            return;
        }
        //input.whenAny((e, event) -> handleInternalAction(config.getEventActions(event)));
        input.whenAny(event -> {
            if (event.getState() == ButtonState.RELEASE) {
                return;
            }
            commandExecutor.addInternalCommand(
                    new ManagerCommandInternal(config.getEventActions(InputSwitchAction.valueOf(event.getState().name())))
            );
        });
    }

    private void registerInternalActions(InputSensorConfig config, Binary input) {
        if (config.getActions().isEmpty()) {
            return;
        }
        //input.whenAny((e, event) -> handleInternalAction(config.getEventActions(event)));
        input.whenAny(event -> commandExecutor.addInternalCommand(
                new ManagerCommandInternal(config.getEventActions(InputSensorAction.valueOf(event.getState().name())))
        ));
    }

    private void handleMqttCommand(String topic, byte[] payload) {
        Logger.info("handle mqtt command: {} payload: {}", topic, payloadToString(payload));
        String[] levels = topic.split("/");
        try {
            SbcDeviceType sbcDeviceType = SbcDeviceType.valueOf(levels[2].toUpperCase());
            String actuatorId = levels[3];
            String command = levels[4];
            String action = payloadToString(payload);
            switch (sbcDeviceType) {
                case RELAY:
                    handleMqttCommandRelay(actuatorId, command, action);
                    break;
                case COVER:
                default:
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.error("Part of topic is missing. Not invoking command.");
        }

    }

    private void handleMqttCommandRelay(String name, String command, String action) {
        if (actuatorMap.containsKey(name)) {
            Actuator<?, ?, ?> actuator = actuatorMap.get(name);
            if (actuator instanceof Relay) {
                ActionOutputType actionOutputType = ActionOutputType.valueOf(action.toUpperCase());
                handleRelayAction((Relay) actuator, actionOutputType, command);
            } else if (actuator instanceof Led) {
                handleLedAction((Led) actuator, action, command);
            }
        } else {
            Logger.error("Target device not found {}", name);
        }
    }

    private void handleInternalAction(List<ActionConfig> actions) {
        actions.forEach(actionConfig -> {
            ActionType action = actionConfig.getAction();
            Logger.info("handleInternalAction: {}", actionConfig);
            switch (action) {
                case MQTT:
                    handleMqttAction((MqttActionConfig) actionConfig);
                    break;
                case OUTPUT:
                    handleOutputAction((OutputActionConfig) actionConfig);
                    break;
                case COVER:
                    handleCoverAction((CoverActionConfig) actionConfig);
                    break;
            }
        });
    }

    private void handleMqttAction(MqttActionConfig action) {
        var topic = action.getTopic();
        var payload = action.getActionMqttMsg();
        if (topic != null && payload != null) {
            mqttHelper.publish(topic, payload.getBytes(), false);
        }
    }

    private void handleOutputAction(OutputActionConfig action) {
        //String pin = action.getPin().replace(" ", "");
        String output = String.valueOf(action.getOutput());
        if (actuatorMap.containsKey(output)) {
            Actuator<?, ?, ?> actuator = actuatorMap.get(output);
            if (actuator instanceof Relay) {
                handleRelayAction((Relay) actuator, action.getActionOutput(), SET);
            } else if (actuator instanceof Led) {
                handleLedAction((Led) actuator, action.getActionOutput().name(), SET);
            }
        } else {
            Logger.error("can't handle action: {} output: {}", action.getAction(), output);
        }
    }

    private void handleCoverAction(CoverActionConfig action) {
        Logger.error("not implemented yet action: {}", action.getAction());
    }

    private void sendHaAutodiscovery(Availability availability) {
        var topicPrefix = availability.getTopicPrefix();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(availability);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var topic = Availability.formatTopic(
                mqttConfigHelper.getHaDiscoveryPrefix(),
                availability.getHaDeviceTypeName(),
                topicPrefix,
                availability.getNodeName(),
                CONFIG
        );
        Logger.info("Sending HA discovery for {}, id:{} : {}.", availability.getHaDeviceType(), availability.getId(), availability.getName());
        mqttConfigHelper.addAutodiscoveryMsg(topic, payload);

        mqttHelper.publish(topic, payload.getBytes());
    }


    @Override
    public void close() {
        sendStatus(DeviceState.OFFLINE);

        sensorMap.forEach((s, sensor) -> {
            if (sensor instanceof ModbusSensor) {
                sensor.close();
                sendStatus(DeviceState.OFFLINE, ((ModbusSensor) sensor).getId());
            }
        });
    }

    @Override
    public void accept(ManagerCommand command) {
        try {


            if (command instanceof ManagerCommandInternal) {
                handleInternalAction(
                        ((ManagerCommandInternal) command).getEventActions()
                );
            } else if (command instanceof ManagerCommandExternal) {
                handleMqttCommand(
                        ((ManagerCommandExternal) command).getTopic(),
                        ((ManagerCommandExternal) command).getPayloadAsBytes()
                );
            }
        } catch (RuntimeException e) {
            Logger.error(e);
        }
    }

    public <B extends Bus<?>> B getBus(PlatformType type, String id, Class<B> returnClass) {
        return busMap.stream()
                .filter(bus -> bus.getPlatformType() == type)
                .filter(bus -> bus.getId().equals(id))
                .map(returnClass::cast)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public void onConnected(boolean reconnect) {

    }

    @Override
    public void onDisconnect() {

    }

}
