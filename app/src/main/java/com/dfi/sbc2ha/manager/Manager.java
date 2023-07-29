package com.dfi.sbc2ha.manager;

import com.dfi.sbc2ha.App;
import com.dfi.sbc2ha.EasingOld;
import com.dfi.sbc2ha.EasingType;
import com.dfi.sbc2ha.EasingVariant;
import com.dfi.sbc2ha.actuator.*;
import com.dfi.sbc2ha.bus.Bus;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.LoggerConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.ActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.CoverActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.MqttActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.action.OutputActionConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionOutputType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.OledConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.PlatformConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.BusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.*;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.actuator.CoverEvent;
import com.dfi.sbc2ha.event.actuator.LedEvent;
import com.dfi.sbc2ha.event.actuator.LedFadingEvent;
import com.dfi.sbc2ha.event.actuator.RelayEvent;
import com.dfi.sbc2ha.event.sensor.ModbusEvent;
import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.dfi.sbc2ha.helper.StateManager;
import com.dfi.sbc2ha.helper.bus.BusFactory;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.message.*;
import com.dfi.sbc2ha.helper.ha.command.LightCommand;
import com.dfi.sbc2ha.helper.stats.StatsProvider;
import com.dfi.sbc2ha.platform.mqtt.MqttPlatform;
import com.dfi.sbc2ha.platform.oled.Display;
import com.dfi.sbc2ha.platform.oled.DisplayFactory;
import com.dfi.sbc2ha.sensor.BinarySensor;
import com.dfi.sbc2ha.sensor.ScalarSensor;
import com.dfi.sbc2ha.sensor.Sensor;
import com.dfi.sbc2ha.sensor.SensorFactory;
import com.dfi.sbc2ha.sensor.binary.Binary;
import com.dfi.sbc2ha.sensor.modbus.ModbusSensor;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.dfi.sbc2ha.state.device.DeviceState;
import com.dfi.sbc2ha.state.sensor.ButtonState;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dfi.sbc2ha.platform.mqtt.hivemq.MqttHelperHiveRx.payloadToString;

@Slf4j
public class Manager implements AutoCloseable {

    private final Map<String, Sensor> sensorMap = new HashMap<>();
    private final Map<String, Actuator> actuatorMap = new HashMap<>();
    private final List<Bus<?>> busMap = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StateManager stateManager;
    private final ManagerExecutor commandExecutor;
    private final AppConfig config;
    private final MqttPlatform mqttPlatform;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private Display display;
    private StatsProvider statsProvider;


    public Manager(AppConfig config) {
        this.config = config;

        setupBoard(config);
        stateManager = StateManager.getInstance();
        commandExecutor = new ManagerExecutor(this::handleCommand);

        setupDisplay(config.getPlatform(PlatformType.OLED, OledConfig.class), config.getActuatorLabels());


        mqttPlatform = new MqttPlatform(this, config.mqttConfig());
        mqttPlatform.start();

        registerBuses();

        mqttPlatform.waitForMqttReadyForPublish();
        registerActuators();
        registerSensors();
        registerLoggerField();

        subscribeMqtt();


        if (statsProvider != null) {
            addDisplayLine("running stats provider");
            statsProvider.start();

            if (null != display) {
                display.setStarted();
                addDisplayLine("ready");
                display.handleClick(null);
            }
        }
        Diozero.registerForShutdown(this);
    }

    private void setupBoard(AppConfig config) {
        log.debug("Setup board");
        addDisplayLine("setup board");
        try {
            NativeDeviceFactoryInterface ndf = DeviceFactoryHelper.getNativeDeviceFactory();
            BoardInfo boardInfo = ndf.getBoardInfo();

            if (config.getExtensionBoards() != null) {
                ExtensionBoardInfo.initialize(config.getExtensionBoards().getVendor(), config.getExtensionBoards().getInputBoard(), config.getExtensionBoards().getOutputBoard());
            }


            log.info("Found SBC name: {}  mem: {}", boardInfo.getLongName(), boardInfo.getMemoryKb());
        } catch (RuntimeException e) {
            log.error("Setup board failed", e);
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


    private void registerBuses() {
        addDisplayLine("prepare buses");
        for (PlatformConfig busConfig : config.getPlatform()) {
            if (busConfig instanceof BusConfig) {
                addDisplayLine("prepare bus from config" + busConfig.getClass().getSimpleName());
                try {
                    busMap.add(BusFactory.createBus((BusConfig) busConfig));
                } catch (RuntimeException logged) {
                    log.error("unable setup modbus: {} :{}", ((BusConfig) busConfig).getBusId(), logged.getMessage());
                }
            }
        }
    }

    private void registerActuators() {
        addDisplayLine("prepare actuators");
        for (ActuatorConfig actuatorConfig : config.getActuator()) {
            try {
                Actuator actuator = ActuatorFactory.getActuator(actuatorConfig, busMap);
                addActuator(actuator);
                registerStateManager(actuator, actuatorConfig);
                registerStateMqtt(actuator, actuatorConfig);

            } catch (RuntimeException logged) {
                log.error("unable setup actuator: {} :{}", actuatorConfig.getName(), logged.getMessage());
            }
        }
    }

    private void registerSensors() {
        addDisplayLine("prepare sensors");
        for (SensorConfig sensorConfig : config.getSensor()) {
            try {
                Sensor sensor = SensorFactory.create(sensorConfig, sensorMap, busMap);
                addSensor(sensor);

                if (sensor instanceof BinarySensor) {
                    registerInternalActions(sensorConfig, sensor);
                }
                registerStateMqtt(sensorConfig, sensor);

            } catch (RuntimeException e) {
                log.error("unable setup sensor: {} :{}", sensorConfig.getName(), e.getMessage());
            }
        }
    }

    private void registerStateManager(Actuator actuator, ActuatorConfig actuatorConfig) {
        if (actuatorConfig.isRestoreState()) {
            actuator.addListenerAny(actuatorEvent -> stateManager.handlerState(actuatorConfig.getOutputType(), actuator, actuatorEvent));
        } else {
            stateManager.remove(actuatorConfig.getOutputType(), actuator.getName());
        }
    }

    private void registerStateMqtt(Actuator actuator, ActuatorConfig actuatorConfig) {
        if (mqttPlatform.isEnabled()) {
            Availability availability = Availability.getActuatorAvailability(actuatorConfig);
            if (actuatorConfig.isShowInHa()) {
                mqttPlatform.sendHaAutodiscovery(availability);
            }
            actuator.whenAny(event -> publishActuatorEvent(availability, event));
            publishInitialActuatorEvent(actuator, actuatorConfig, availability);
        }
    }

    private void registerStateMqtt(SensorConfig sensorConfig, Sensor sensor) {
        if (mqttPlatform.isEnabled()) {
            Availability availability = Availability.getSensorAvailability(sensorConfig);
            if (sensorConfig.isShowInHa()) {
                mqttPlatform.sendHaAutodiscovery(availability);
            }
            if (sensor instanceof ScalarSensor) {
                sensor.whenAny(event -> publishSensorState(availability, event));
                sendInitialScalarState(((ScalarSensor) sensor).getValue(), availability.getStateTopic());
            } else if (sensor instanceof BinarySensor) {
                sensor.whenAny(event -> publishSensorState(availability, event));
                if (sensor instanceof Binary) {
                    publishSensorState(availability, ((Binary) sensor).getInitialState());
                }
            } else if (sensor instanceof ModbusSensor) {
                sensor.whenAny(event -> publishModbusSensorState((ModbusSensorAvailability) availability, (ModbusEvent) event));
                ((ModbusSensor) sensor).whenState(state -> mqttPlatform.sendStatus(state, ((ModbusSensor) sensor).getId()));
                mqttPlatform.sendStatus(DeviceState.ONLINE, ((ModbusSensor) sensor).getId());
            } else {
                throw new RuntimeException("unknown sensor");
            }
        }
    }


    private void registerInternalActions(SensorConfig config, Sensor sensor) {
        if (((InputConfig<?>) config).getActions().isEmpty()) {
            return;
        }
        sensor.whenAny(event -> {
            if (event.getState().equals(ButtonState.RELEASE.name())) {
                return;
            }

            List<ActionConfig> eventActions;
            if (config instanceof InputSwitchConfig) {
                eventActions = ((InputSwitchConfig) config).getEventActions(InputSwitchAction.valueOf(event.getState()));
            } else {
                eventActions = ((InputSensorConfig) config).getEventActions(InputSensorAction.valueOf(event.getState()));
            }

            commandExecutor.addInternalCommand(new ManagerCommandInternal(eventActions)
            );
        });
    }


    private void subscribeMqtt() {
        if (mqttPlatform.isEnabled()) {
            addDisplayLine("subscribe mqtt");
            mqttPlatform.subscribeHaDiscovery();
            mqttPlatform.subscribeHaCommands(this::handleHaCommands);
            mqttPlatform.subscribeSelfState(this::handleSelfState);

            mqttPlatform.sendStatus(DeviceState.ONLINE);
        }
    }

    public void addDisplayLine(String line) {
        //TODO refactor to eventListener
        if (display != null) {
            log.trace("display add: {}", line);
            display.addBootLine(line);
        }
    }

    /////////////////////////////////////////////////////////////


    private void publishModbusSensorState(ModbusSensorAvailability availability, ModbusEvent event) {
        String payload = String.valueOf(event.getValue());
        mqttPlatform.publish(availability.getStateTopic(event.getRegister()), payload.getBytes(), false);
    }

    private void publishSensorState(Availability availability, StateEvent event) {
        String payload;
        if (event instanceof ScalarEvent) {
            payload = String.valueOf(((ScalarEvent) event).getValue());
        } else {
            payload = event.getState().toString().toLowerCase();
        }
        mqttPlatform.publish(availability.getStateTopic(), payload.getBytes(), false);
    }


    private void publishActuatorState(String topic, StateEvent evt) {
        String payload;
        try {
            SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter.serializeAllExcept("type", "step", "easingType", "easingVariant");
            SimpleFilterProvider filters = new SimpleFilterProvider();
            filters.setDefaultFilter(theFilter);
            payload = objectMapper.writer(filters).writeValueAsString(evt);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        mqttPlatform.publish(topic, payload.getBytes(), false);
    }

    private void publishInitialActuatorEvent(Actuator actuator, ActuatorConfig actuatorConfig, Availability availability) {
        StateEvent evt = stateManager.get(actuatorConfig.getOutputType(), actuator.getName());
        if (evt == null) {
            if (actuator instanceof Relay) {
                evt = new RelayEvent(ActuatorState.OFF);
            }
        }
        publishActuatorEvent(availability, evt);
    }


    private void publishActuatorEvent(Availability availability, StateEvent evt) {
        if (evt == null) {
            return;
        }
        if (evt instanceof LedEvent) {
            if (((LedEvent) evt).getBrightness() == 0 && evt.getState().equals(ActuatorState.ON.name())) {
                return;
            }
            publishActuatorState(availability.getStateTopic(), getPayloadFromStateEvent(evt));
        } else if (evt instanceof LedFadingEvent) {
            if (((LedFadingEvent) evt).getBrightness() == 0 && evt.getState().equals(ActuatorState.ON.name())) {
                return;
            }
            publishActuatorState(availability.getStateTopic(), getPayloadFromStateEvent(evt));
        } else if (evt instanceof RelayEvent) {
            publishActuatorState(availability.getStateTopic(), evt.getState().toLowerCase());
        } else if (evt instanceof CoverEvent) {
            //TODO split event to pos i state
            publishActuatorState(availability.getStateTopic(), getPayloadFromStateEvent(evt));
            publishActuatorState(((CoverAvailability) availability).getPositionTopic(), getPayloadFromStateEvent(evt));

        } else {
            throw new RuntimeException("unknown event: " + evt.getClass().getSimpleName());
        }
    }


    private void publishActuatorState(String topic, String payload) {
        mqttPlatform.publish(topic, payload.getBytes(), false);
    }

    private String getPayloadFromStateEvent(StateEvent event) {
        try {
            SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter.serializeAllExcept("type", "step", "easingType", "easingVariant");
            SimpleFilterProvider filters = new SimpleFilterProvider();
            filters.setDefaultFilter(theFilter);
            return objectMapper.writer(filters).writeValueAsString(event);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private void sendInitialScalarState(Float initialValue, String topic) {
        if (initialValue.isNaN()) {
            return;
        }
        String payload = initialValue.toString();
        mqttPlatform.publish(topic, payload.getBytes(), false);
    }


    private void handleSelfState(String topic, byte[] payload) {
        String state = new String(payload).toUpperCase();
        if (DeviceState.valueOf(state) == DeviceState.OFFLINE && !shuttingDown.get()) {
            mqttPlatform.sendStatus(DeviceState.ONLINE);
        }
    }

    private void handleHaCommands(String topic, byte[] payload) {
        log.trace("Received publish topic:  {}, payload: {}", topic, payloadToString(payload));
        commandExecutor.addExternalCommand(new ManagerCommandExternal(topic, payload));
    }


    public void handleCommand(ManagerCommand command) {
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
            log.error("execute command failed", e);
        }
    }

    private void handleMqttCommand(String topic, byte[] payload) {
        log.info("handle mqtt command: {} payload: {}", topic, payloadToString(payload));
        String[] levels = topic.split("/");
        try {
            SbcDeviceType sbcDeviceType = SbcDeviceType.valueOf(levels[2].toUpperCase());
            String actuatorId = levels[3];
            String action = payloadToString(payload);
            switch (sbcDeviceType) {
                case RELAY:
                    handleMqttCommandRelay(actuatorId, action);
                    break;
                case TEXT:
                    handleMqttCommandText(actuatorId, action);
                    break;
                case COVER:
                    //String command = levels[4];
                    throw new RuntimeException("not implemented yet");
                default:
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Part of topic is missing. Not invoking command.");
        }

    }

    private void handleMqttCommandRelay(String name, String action) {
        if (actuatorMap.containsKey(name)) {
            Actuator actuator = actuatorMap.get(name);
            if (actuator instanceof Relay) {
                ActionOutputType actionOutputType = ActionOutputType.valueOf(action.toUpperCase());
                handleRelayAction((Relay) actuator, actionOutputType);
            } else if (actuator instanceof Led) {
                handleLedAction((Led) actuator, action);
            } else if (actuator instanceof LedFading) {
                handleLedAction((LedFading) actuator, action);
            }
        } else {
            log.error("Target device not found {}", name);
        }
    }

    private void handleLedAction(Led actuator, String action) {
        LightCommand lightCommand;
        try {
            lightCommand = objectMapper.readValue(action, LightCommand.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (lightCommand.getTransition() > 0 && lightCommand.getEffect() == null) {
            lightCommand.setEffect(EasingOld.linerIn.name());
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

    private void handleLedAction(LedFading actuator, String action) {
        LightCommand lightCommand;
        try {
            lightCommand = objectMapper.readValue(action, LightCommand.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (lightCommand.getTransition() > 0 && lightCommand.getEffect() == null) {
            lightCommand.setEffect(EasingType.LINEAR + "-" + (lightCommand.getState().equals(LightCommand.ON) ? EasingVariant.IN : EasingVariant.OUT));
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

    private void handleRelayAction(Relay actuator, ActionOutputType actionOutputType) {
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

    private void handleInternalAction(List<ActionConfig> actions) {
        for (ActionConfig actionConfig : actions) {
            ActionType action = actionConfig.getAction();
            log.info("Internal action: {}", actionConfig);
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
        }
    }

    private void handleMqttAction(MqttActionConfig action) {
        var topic = action.getTopic();
        var payload = action.getActionMqttMsg();
        if (topic != null && payload != null) {
            mqttPlatform.publish(topic, payload.getBytes(), false);
        }
    }

    private void handleOutputAction(OutputActionConfig action) {
        //String pin = action.getPin().replace(" ", "");
        String output = String.valueOf(action.getOutput());
        if (actuatorMap.containsKey(output)) {
            Actuator actuator = actuatorMap.get(output);
            if (actuator instanceof Relay) {
                handleRelayAction((Relay) actuator, action.getActionOutput());
            } else if (actuator instanceof Led) {
                handleLedAction((Led) actuator, action.getActionOutput().name());
            }
        } else {
            log.error("can't handle action: {} output: {}", action.getAction(), output);
        }
    }

    private void handleCoverAction(CoverActionConfig action) {
        log.error("not implemented yet action: {}", action.getAction());
    }

    @Override
    public void close() {
        shuttingDown.set(true);
        mqttPlatform.unSubscribeSelfState();
        mqttPlatform.sendStatus(DeviceState.OFFLINE);

        sensorMap.forEach((s, sensor) -> {
            if (sensor instanceof ModbusSensor) {
                sensor.close();
                mqttPlatform.sendStatus(DeviceState.OFFLINE, ((ModbusSensor) sensor).getId());
            }
        });

    }

    private void addSensor(Sensor sensor) {
        if (sensorMap.containsKey(sensor.getName())) {
            throw new IllegalArgumentException("sensor with name " + sensor.getName() + " already exist");
        }
        sensorMap.put(sensor.getName(), sensor);
    }

    private void addActuator(Actuator actuator) {
        String id = String.valueOf(actuator.getId());
        if (actuatorMap.containsKey(id)) {
            throw new IllegalArgumentException("actuator with name " + actuator.getName() + " already exist");
        }
        actuatorMap.put(id, actuator);
    }


    ////tests

    private void handleMqttCommandText(String name, String action) {
        if (actuatorMap.containsKey(name)) {
            Actuator actuator = actuatorMap.get(name);
            if (actuator instanceof TextField) {
                if (action.contains("=")) {
                    String[] split = action.split("=");
                    var log = split[0].trim();
                    var level = split[1].trim();

                    LoggerConfig configLogger = config.getLogger();
                    configLogger.getLogs().put(log, level);

                    App.configureLogging(configLogger);


                }
            }
        } else {
            log.error("Target device not found {}", name);
        }
    }

    private void registerLoggerField() {
        TextField actuator = ActuatorFactory.getLoggerField();

        TextFieldAvailability availability = Availability.getTextFieldActuatorAvailability(String.valueOf(actuator.getId()), actuator.getName());
        mqttPlatform.sendHaAutodiscovery(availability);
        actuatorMap.put(String.valueOf(actuator.getId()), actuator);

    }

}

