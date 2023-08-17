import {PlatformType} from "../../../../definition/enums/PlatformType";
import {ComponentType} from "@angular/cdk/portal";
import {PlatformMqttComponent} from "../config/platform/platform-mqtt/platform-mqtt.component";
import {PlatformOledComponent} from "../config/platform/platform-oled/platform-oled.component";
import {MqttConfig} from "../../../../definition/platform/MqttConfig";
import {OledConfig} from "../../../../definition/platform/OledConfig";
import {DallasBusConfig} from "../../../../definition/platform/bus/DallasBusConfig";
import {Ds2482BusConfig} from "../../../../definition/platform/bus/Ds2482BusConfig";
import {Lm75BusConfig} from "../../../../definition/platform/bus/Lm75BusConfig";
import {Mcp23017BusConfig} from "../../../../definition/platform/bus/Mcp23017BusConfig";
import {ModbusBusConfig} from "../../../../definition/platform/bus/ModbusBusConfig";
import {PlatformBusComponent} from "../config/platform/platform-bus/platform-bus.component";
import {PlatformI2cComponent} from "../config/platform/platform-i2c/platform-i2c.component";
import {PlatformModbusComponent} from "../config/platform/platform-modbus/platform-modbus.component";
import {PCA9685BusConfig} from "../../../../definition/platform/bus/PCA9685BusConfig";

export const selectableDefinition: SelectablePlatformItem[] = [{
  type: PlatformType.MQTT,
  component: PlatformMqttComponent,
  single: true,
  config: MqttConfig,
}, {
  type: PlatformType.OLED,
  component: PlatformOledComponent,
  single: true,
  config: OledConfig,
}, {
  type: PlatformType.DALLAS,
  component: PlatformBusComponent,
  single: true,
  config: DallasBusConfig
}, {
  type: PlatformType.DS2482,
  component: PlatformI2cComponent,
  single: false,
  config: Ds2482BusConfig
}, {
  type: PlatformType.LM75,
  component: PlatformI2cComponent,
  single: false,
  config: Lm75BusConfig
}, {
  type: PlatformType.MCP23017,
  component: PlatformI2cComponent,
  single: false,
  config: Mcp23017BusConfig
}, {
  type: PlatformType.PCA9685,
  component: PlatformI2cComponent,
  single: false,
  config: PCA9685BusConfig
}, {
  type: PlatformType.MODBUS,
  component: PlatformModbusComponent,
  single: false,
  config: ModbusBusConfig
}];

export interface SelectablePlatformItem {
  type: PlatformType
  component: ComponentType<any>
  single: boolean,
  config: Object
}

