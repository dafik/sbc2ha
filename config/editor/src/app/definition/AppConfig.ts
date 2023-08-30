import {ExtensionBoardsConfig} from "./extentsionBoard/ExtensionBoardsConfig";
import {PlatformConfig} from "./platform/PlatformConfig";
import {SensorConfig} from "./sensor/SensorConfig";
import {ActuatorConfig} from "./actuator/ActuatorConfig";
import {LoggerConfig} from "./LoggerConfig";
import {Expose, Type} from "class-transformer";
import {DallasBusConfig} from "./platform/bus/DallasBusConfig";
import {Mcp23017BusConfig} from "./platform/bus/Mcp23017BusConfig";
import {Ds2482BusConfig} from "./platform/bus/Ds2482BusConfig";
import {I2cBusConfig} from "./platform/bus/I2cBusConfig";
import {Lm75BusConfig} from "./platform/bus/Lm75BusConfig";
import {ModbusBusConfig} from "./platform/bus/ModbusBusConfig";
import {MqttConfig} from "./platform/MqttConfig";
import {OledConfig} from "./platform/OledConfig";
import {DS18B20busFs} from "./sensor/oneWire/therm/fs/DS18B20busFs";
import {DS18B20busDS2482} from "./sensor/oneWire/therm/ds2482/DS18B20busDS2482";
import {InputSensorConfig} from "./sensor/digital/InputSensorConfig";
import {InputSwitchConfig} from "./sensor/digital/InputSwitchConfig";
import {Lm75SensorConfig} from "./sensor/Lm75SensorConfig";
import {ModbusSensorConfig} from "./sensor/ModbusSensorConfig";
import {AnalogSensorConfig} from "./sensor/analog/AnalogSensorConfig";
import {ResistanceConfig} from "./sensor/analog/ResistanceConfig";
import {NtcConfig} from "./sensor/analog/NtcConfig";
import {McpOutputConfig} from "./actuator/McpOutputConfig";
import {PcaOutputConfig} from "./actuator/PcaOutputConfig";
import {GpioOutputConfig} from "./actuator/GpioOutputConfig";
import {GpioPwmOutputConfig} from "./actuator/GpioPwmOutputConfig";
import {CoverConfig} from "./actuator/CoverConfig";
import {Duration} from "./Duration";


export class AppConfig {
    @Expose({name: "extension_boards"})
    @Type(() => ExtensionBoardsConfig)
    extensionBoards?: ExtensionBoardsConfig;
    @Type(() => PlatformConfig, {
        discriminator: {
            property: "platform",
            subTypes: [
                {value: DallasBusConfig, name: "dallas"},
                {value: Mcp23017BusConfig, name: "mcp23017"},
                {value: Ds2482BusConfig, name: "ds2482"},
                {value: I2cBusConfig, name: "i2c"},
                {value: Lm75BusConfig, name: "lm75"},
                {value: ModbusBusConfig, name: "modbus"},
                {value: MqttConfig, name: "mqtt"},
                {value: OledConfig, name: "oled"},
            ]
        }
    })
    platform: PlatformConfig[] = [];
    @Type(() => SensorConfig, {
        discriminator: {
            property: "platform",
            subTypes: [
                {value: DS18B20busFs, name: "dallas"},
                {value: DS18B20busDS2482, name: "ds2482"},
                {value: InputSensorConfig, name: "digital"},
                {value: InputSwitchConfig, name: "switch"},
                {value: Lm75SensorConfig, name: "lm75"},
                {value: ModbusSensorConfig, name: "modbus"},
                {value: AnalogSensorConfig, name: "analog"},
                {value: ResistanceConfig, name: "resistance"},
                {value: NtcConfig, name: "ntc"}
            ]
        }
    })
    sensor: SensorConfig[] = [];
    @Type(() => ActuatorConfig, {
        discriminator: {
            property: "kind",
            subTypes: [
                {value: McpOutputConfig, name: "mcp"},
                {value: PcaOutputConfig, name: "pca"},
                {value: GpioOutputConfig, name: "gpio"},
                {value: GpioPwmOutputConfig, name: "gpiopwm"},
                {value: CoverConfig, name: "cover"}
            ]
        }
    })
    actuator: ActuatorConfig[] = [];
    logger?: LoggerConfig;
}

export function toDuration(value: any, defaultValue?: string) {
    if (value) {
        if (value.hasOwnProperty("duration")) {
            value = value.duration
        }
        return new Duration(value);
    } else if (defaultValue) {
        return new Duration(defaultValue);
    }
    return value;
}

export function fromDuration(value: any) {
    if (value) {
        if (value instanceof Duration) {
            return value.duration
        }
        return value;
    }
}

export function skipDefault(value: any, defaultValue: any) {
    if (value instanceof Object) {
        return deepEqual(value, defaultValue) ? undefined : value
    }
    if (value !== defaultValue) {
        return value
    }
}

export function addDefault(value: any, defaultValue: any) {
    if (value === undefined) {
        return defaultValue
    }
    return value;
}

function deepEqual(x: any, y: any): boolean {
    const ok = Object.keys, tx = typeof x, ty = typeof y;
    return x && y && tx === 'object' && tx === ty ? (
        ok(x).length === ok(y).length &&
        ok(x).every(key => deepEqual(x[key], y[key]))
    ) : (x === y);
}
