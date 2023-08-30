import {Injectable} from '@angular/core';
import {AppConfig} from "../definition/AppConfig";
import {ExtensionBoardsConfig} from "../definition/extentsionBoard/ExtensionBoardsConfig";
import {PlatformConfig} from "../definition/platform/PlatformConfig";
import {SensorConfig} from "../definition/sensor/SensorConfig";
import {ActuatorConfig} from "../definition/actuator/ActuatorConfig";
import {LoggerConfig} from "../definition/LoggerConfig";
import {PlatformType} from "../definition/enums/PlatformType";
import {MqttConfig} from "../definition/platform/MqttConfig";
import {OledConfig} from "../definition/platform/OledConfig";
import {Mcp23017BusConfig} from "../definition/platform/bus/Mcp23017BusConfig";
import {PCA9685BusConfig} from "../definition/platform/bus/PCA9685BusConfig";
import {DallasBusConfig} from "../definition/platform/bus/DallasBusConfig";
import {Ds2482BusConfig} from "../definition/platform/bus/Ds2482BusConfig";
import {ModbusBusConfig} from "../definition/platform/bus/ModbusBusConfig";
import {Lm75BusConfig} from "../definition/platform/bus/Lm75BusConfig";
import {Lm75SensorConfig} from "../definition/sensor/Lm75SensorConfig";
import {Filter} from "../definition/sensor/ScalarSensorConfig";
import {InputConfig} from "../definition/sensor/digital/InputConfig";
import {ActionsSwitch, InputSwitchConfig} from "../definition/sensor/digital/InputSwitchConfig";
import {Duration} from "../definition/Duration";
import {ActionConfig} from "../definition/action/ActionConfig";
import {ActionType} from "../definition/enums/ActionType";
import {MqttActionConfig} from "../definition/action/MqttActionConfig";
import {OutputActionConfig} from "../definition/action/OutputActionConfig";
import {CoverActionConfig} from "../definition/action/CoverActionConfig";
import {InputSwitchAction} from "../definition/enums/InputSwitchAction";
import {ActionsSensor, InputSensorConfig} from "../definition/sensor/digital/InputSensorConfig";
import {InputSensorAction} from "../definition/enums/InputSensorAction";
import {DS18B20busFs} from "../definition/sensor/oneWire/therm/fs/DS18B20busFs";
import {DS18B20busDS2482} from "../definition/sensor/oneWire/therm/ds2482/DS18B20busDS2482";
import {AnalogSensorConfig} from "../definition/sensor/analog/AnalogSensorConfig";
import {ModbusSensorConfig} from "../definition/sensor/ModbusSensorConfig";
import {ResistanceConfig} from "../definition/sensor/analog/ResistanceConfig";
import {BconstantCalibration, NtcConfig, SetPoint, ValueCalibration} from "../definition/sensor/analog/NtcConfig";
import {OutputKindType} from "../definition/enums/OutputKindType";
import {GpioOutputConfig} from "../definition/actuator/GpioOutputConfig";
import {ActuatorType} from "../definition/enums/ActuatorType";
import {McpOutputConfig} from "../definition/actuator/McpOutputConfig";
import {PcaOutputConfig} from "../definition/actuator/PcaOutputConfig";
import {CoverConfig} from "../definition/actuator/CoverConfig";
import {GpioPwmOutputConfig} from "../definition/actuator/GpioPwmOutputConfig";

@Injectable({
    providedIn: 'root'
})
export class AppConfigFromYamlService {

    constructor() {
    }

    public fromYaml(src: any): AppConfig {
        let config = new AppConfig();
        config.extensionBoards = this.extensionBoard(src.extension_boards)
        if (src.platform) {
            config.platform = this.platform(src.platform);
        }
        if (src.sensor) {
            config.sensor = this.sensor(src.sensor);
        }
        if (src.actuator) {
            config.actuator = this.actuator(src.actuator);
        }
        if (src.logger) {
            config.logger = this.logger(src.logger);
        }

        return config
    }

    private extensionBoard(src: any): ExtensionBoardsConfig {
        return new ExtensionBoardsConfig(src.vendor, src.input_board, src.output_board);
    }

    private platform(src: any[]): PlatformConfig[] {
        const platform: PlatformConfig[] = [];

        src.forEach(value => {
            switch (value.platform as PlatformType) {
                case PlatformType.MQTT:
                    platform.push(this.platformMqtt(value));
                    break;
                case PlatformType.OLED:
                    platform.push(this.platformOled(value));
                    break;
                case PlatformType.MCP23017:
                    platform.push(this.platformMcp(value));
                    break;
                case PlatformType.PCA9685:
                    platform.push(this.platformPca(value));
                    break;
                case PlatformType.DALLAS:
                    platform.push(this.platformDallas(value));
                    break;
                case PlatformType.DS2482:
                    platform.push(this.platformDs2482(value));
                    break;
                case PlatformType.MODBUS:
                    platform.push(this.platformModbus(value));
                    break;
                case PlatformType.LM75:
                    platform.push(this.platformLm75(value));
                    break;
                default:
                    throw "err"
            }
        })

        return platform
    }

    private sensor(src: any[]): SensorConfig[] {
        const sensor: SensorConfig[] = [];
        if (!src?.length) {
            return sensor;
        }

        src.forEach(value => {
            switch (value.platform as PlatformType) {
                case PlatformType.GPIO:
                    sensor.push(this.sensorGpio(value))
                    break;
                case PlatformType.DALLAS:
                    sensor.push(this.sensorDallas(value))
                    break;
                case PlatformType.DS2482:
                    sensor.push(this.sensorDs2482(value))
                    break;
                case PlatformType.ANALOG:
                    sensor.push(this.sensorAnalog(value))
                    break;
                case PlatformType.MODBUS:
                    sensor.push(this.sensorModbus(value))
                    break;
                case PlatformType.LM75:
                    sensor.push(this.sensorLm75(value))
                    break;
                case PlatformType.DIGITAL:
                    sensor.push(this.sensorDigital(value))
                    break;
                case PlatformType.SWITCH:
                    sensor.push(this.sensorSwitch(value));
                    break;
                case PlatformType.RESISTANCE:
                    sensor.push(this.sensorResistance(value))
                    break;
                case PlatformType.NTC:
                    sensor.push(this.sensorNtc(value))
                    break;
                default:
                    throw "err";
            }
        })
        return sensor
    }

    private actuator(src: any[]): ActuatorConfig[] {
        const actuator: ActuatorConfig[] = [];

        src.forEach(value => {
            switch (value.kind as OutputKindType) {
                case OutputKindType.GPIO:
                    actuator.push(this.actuatorGpio(value))
                    break;
                case OutputKindType.GPIOPWM:
                    actuator.push(this.actuatorGpioPwm(value))
                    break;
                case OutputKindType.MCP:
                    actuator.push(this.actuatorMcp(value))
                    break;
                case OutputKindType.PCA:
                    actuator.push(this.actuatorPca(value))
                    break;
                case OutputKindType.COVER:
                    actuator.push(this.actuatorCover(value))
                    break;

            }
        })
        return actuator;
    }

    private logger(src: any): LoggerConfig | undefined {
        return src;
    }

    private platformMqtt(value: any) {
        let mqttConfig = new MqttConfig(
            value.host,
            value.username,
            value.password
        );
        if (value.port) {
            mqttConfig.port = value.port
        }
        if (value.topic_prefix) {
            mqttConfig.topicPrefix = value.topic_prefix
        }
        if (value.ha_discovery) {
            if (value.ha_discovery.topic_prefix) {
                mqttConfig.haDiscovery.topicPrefix = value.ha_discovery.topic_prefix
            }
            if (value.ha_discovery.enabled) {
                mqttConfig.haDiscovery.enabled = value.ha_discovery.enabled
            }
        }
        return mqttConfig;
    }

    private platformOled(value: any) {
        let config = new OledConfig()
        if (value.enabled) {
            config.enabled = value.enabled
        }
        if (value.screensaverTimeout) {
            config.screensaverTimeout = value.screensaverTimeout
        }
        if (value.screens) {
            config.screens = value.screens
        }

        return config;
    }

    private platformMcp(value: any) {
        let config = new Mcp23017BusConfig(value.bus_id, value.address)
        return config
    }

    private platformPca(value: any) {
        return new PCA9685BusConfig(value.bus_id, value.address);
    }

    private platformDallas(value: any) {
        return new DallasBusConfig(value.bus_id)
    }

    private platformDs2482(value: any) {
        return new Ds2482BusConfig(value.bus_id, value.address)
    }

    private platformModbus(value: any) {
        return new ModbusBusConfig(value.uart)
    }

    private platformLm75(value: any) {
        return new Lm75BusConfig(value.bus_id, value.address);
    }


    private sensorLm75(value: any) {
        let config = new Lm75SensorConfig(value.name, value.bus_id);
        if (value.updateInterval) {
            config.updateInterval = value.updateInterval
        }
        if (value.showInHa) {
            config.showInHa = value.showInHa
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }
        return config;
    }

    private sensorGpio(value: any): InputConfig<any> {
        let config: InputConfig<any>;
        if (value.kind == 'switch') {
            config = this.sensorSwitch(value);
        } else if (value.kind == 'sensor') {
            config = this.sensorDigital(value);
        } else {
            throw "err";
        }

        return config;
    }

    private sensorDigital(value: any) {
        let config = new InputSensorConfig(value.name, value.input);
        if (value.device_class) {
            config.deviceClass = value.device_class
        }
        if (value.bounce_time) {
            config.bounceTime = new Duration(value.bounce_time);
        }
        if (value.inverted) {
            config.inverted = value.inverted;
        }
        if (value.actions) {
            config.actions = this.actionsSensor(value.actions)
        }
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        return config;
    }

    private sensorSwitch(value: any) {
        let config = new InputSwitchConfig(value.name, value.input);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.click_detection) {
            config.clickDetection = value.click_detection
        }
        if (value.bounce_time) {
            config.bounceTime = new Duration(value.bounce_time);
        }
        if (value.inverted) {
            config.inverted = value.inverted;
        }
        if (value.actions) {
            config.actions = this.actionsSwitch(value.actions)
        }
        return config;
    }

    private actionsSwitch(actions: any) {
        let conf: ActionsSwitch = {};
        if (actions[InputSwitchAction.SINGLE]) {
            conf[InputSwitchAction.SINGLE] = this.action(actions[InputSwitchAction.SINGLE])
        }
        if (actions[InputSwitchAction.DOUBLE]) {
            conf[InputSwitchAction.DOUBLE] = this.action(actions[InputSwitchAction.DOUBLE])
        }
        if (actions[InputSwitchAction.LONG]) {
            conf[InputSwitchAction.LONG] = this.action(actions[InputSwitchAction.LONG])
        }
        return conf;
    }

    private actionsSensor(actions: any) {
        let conf: ActionsSensor = {};
        if (actions[InputSensorAction.PRESSED]) {
            conf[InputSensorAction.PRESSED] = this.action(actions[InputSensorAction.PRESSED])
        }
        if (actions[InputSensorAction.RELEASED]) {
            conf[InputSensorAction.RELEASED] = this.action(actions[InputSensorAction.RELEASED])
        }
        return conf;
    }

    private action(actions: any[]) {
        let conf: ActionConfig[] = [];
        actions.forEach(value => {
            switch (value.action as ActionType) {
                case ActionType.MQTT:
                    conf.push(new MqttActionConfig(value.topic, value.action_mqtt_msg))
                    break;
                case ActionType.OUTPUT:
                    conf.push(new OutputActionConfig(value.output, value.action_output))
                    break;
                case ActionType.COVER:
                    conf.push(new CoverActionConfig(value.output, value.action_cover))
                    break;
            }
        })
        return conf;
    }

    private sensorDallas(value: any) {
        let config = new DS18B20busFs(value.name, value.bus_id, value.address);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.updateInterval) {
            config.updateInterval = value.updateInterval
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }

        return config;
    }

    private sensorDs2482(value: any) {
        let config = new DS18B20busDS2482(value.name, value.bus_id, value.address);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.updateInterval) {
            config.updateInterval = value.updateInterval
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }

        return config;
    }

    private sensorAnalog(value: any) {
        let config = new AnalogSensorConfig(value.name, value.analog);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.updateInterval) {
            config.updateInterval = value.updateInterval
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }

        return config;
    }

    private sensorModbus(value: any) {
        let config = new ModbusSensorConfig(value.name, value.bus_id, value.address, value.model);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.updateInterval) {
            config.updateInterval = value.updateInterval
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }

        return config;
    }

    private sensorResistance(value: any) {
        let config = new ResistanceConfig(value.name, value.sensor, value.direction, value.resistor, value.referenceVoltage);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }
        return config;
    }

    private sensorNtc(value: any) {
        let calibration: ValueCalibration | BconstantCalibration;
        if (value.calibration.bConstant) {
            calibration = new BconstantCalibration(value.b_constant, value.reference_temperature, value.reference_resistance)
        } else {
            calibration = new ValueCalibration(
                new SetPoint('1', '1'),
                new SetPoint('1', '1'),
                new SetPoint('1', '1')
            )
        }
        let config = new NtcConfig(value.name, value.sensor, calibration);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.filters) {
            config.filters = this.filters(value.filters)
        }
        return config;
    }

    private filters(filters: any[]) {
        let configF: Filter[] = [];
        if (filters && filters.length) {
            filters.forEach(value => {
                //TODO filters
            })
        }

        return configF;
    }

    private actuatorGpio(value: any) {
        let config: GpioOutputConfig = new GpioOutputConfig(value.name, value.output_type, value.output);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.restore_state) {
            config.restoreState = value.restore_state
        }
        if (value.momentary_turn_on) {
            config.momentaryTurnOn = new Duration(value.momentary_turn_on)
        }
        if (value.momentary_turn_off) {
            config.momentaryTurnOff = new Duration(value.momentary_turn_off)
        }

        return config;

    }

    private actuatorGpioPwm(value: any) {
        let config: GpioOutputConfig = new GpioPwmOutputConfig(value.name, ActuatorType.LED, value.output);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.restore_state) {
            config.restoreState = value.restore_state
        }
        if (value.momentary_turn_on) {
            config.momentaryTurnOn = new Duration(value.momentary_turn_on)
        }
        if (value.momentary_turn_off) {
            config.momentaryTurnOff = new Duration(value.momentary_turn_off)
        }

        return config;
    }

    private actuatorMcp(value: any) {
        let config: McpOutputConfig = new McpOutputConfig(value.name, value.output_type, value.output);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.restore_state) {
            config.restoreState = value.restore_state
        }
        if (value.momentary_turn_on) {
            config.momentaryTurnOn = new Duration(value.momentary_turn_on)
        }
        if (value.momentary_turn_off) {
            config.momentaryTurnOff = new Duration(value.momentary_turn_off)
        }

        return config;
    }

    private actuatorPca(value: any) {
        let config: PcaOutputConfig = new PcaOutputConfig(value.name, value.output_type, value.output);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.restore_state) {
            config.restoreState = value.restore_state
        }
        if (value.momentary_turn_on) {
            config.momentaryTurnOn = new Duration(value.momentary_turn_on)
        }
        if (value.momentary_turn_off) {
            config.momentaryTurnOff = new Duration(value.momentary_turn_off)
        }

        return config;
    }

    private actuatorCover(value: any) {
        let config: CoverConfig = new CoverConfig(value.name, value.output_type, value.output,
            value.open_relay, value.open_relay_bus_id, value.open_relay_bus_type, value.close_relay, value.close_relay_bus_id, value.close_relay_bus_type,
            new Duration(value.open_time),
            new Duration(value.close_time),
            value.devicet_lass);
        if (value.show_in_ha) {
            config.showInHa = value.show_in_ha;
        }
        if (value.restore_state) {
            config.restoreState = value.restore_state
        }
        if (value.momentary_turn_on) {
            config.momentaryTurnOn = new Duration(value.momentary_turn_on)
        }
        if (value.momentary_turn_off) {
            config.momentaryTurnOff = new Duration(value.momentary_turn_off)
        }

        return config;
    }
}
