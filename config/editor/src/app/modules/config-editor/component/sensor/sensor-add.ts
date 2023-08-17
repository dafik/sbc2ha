import {PlatformType} from "../../../../definition/enums/PlatformType";
import {ComponentType} from "@angular/cdk/portal";
import {ModbusSensorConfig} from "../../../../definition/sensor/ModbusSensorConfig";
import {Lm75SensorConfig} from "../../../../definition/sensor/Lm75SensorConfig";
import {DS18B20busFs} from "../../../../definition/sensor/oneWire/therm/fs/DS18B20busFs";
import {DS18B20busDS2482} from "../../../../definition/sensor/oneWire/therm/ds2482/DS18B20busDS2482";
import {InputSwitchConfig} from "../../../../definition/sensor/digital/InputSwitchConfig";
import {InputSensorConfig} from "../../../../definition/sensor/digital/InputSensorConfig";
import {AnalogSensorConfig} from "../../../../definition/sensor/analog/AnalogSensorConfig";
import {NtcConfig} from "../../../../definition/sensor/analog/NtcConfig";
import {ResistanceConfig} from "../../../../definition/sensor/analog/ResistanceConfig";
import {SensorAnalogComponent} from "../config/sensor/sensor-analog/sensor-analog.component";
import {SensorResistanceComponent} from "../config/sensor/sensor-resistance/sensor-resistance.component";
import {SensorNtcComponent} from "../config/sensor/sensor-ntc/sensor-ntc.component";
import {SensorDigitalComponent} from "../config/sensor/sensor-digital/sensor-digital.component";
import {SensorDs2482Component} from "../config/sensor/sensor-ds2482/sensor-ds2482.component";
import {SensorDallasComponent} from "../config/sensor/sensor-dallas/sensor-dallas.component";
import {SensorLm75Component} from "../config/sensor/sensor-lm75/sensor-lm75.component";
import {SensorModbusComponent} from "../config/sensor/sensor-modbus/sensor-modbus.component";
import {SensorSwitchComponent} from "../config/sensor/sensor-switch/sensor-switch.component";

export const selectableSensorDefinition: SelectableSensorItem[] = [
  {
    type: PlatformType.ANALOG,
    component: SensorAnalogComponent,
    config: AnalogSensorConfig,
  },
  {
    type: PlatformType.RESISTANCE,
    component:SensorResistanceComponent,
    config: ResistanceConfig,
  },
  {
    type: PlatformType.NTC,
    component:SensorNtcComponent,
    config: NtcConfig,
  },
  {
    type: PlatformType.DIGITAL,
    component:SensorDigitalComponent,
    config: InputSensorConfig,
  },
  {
    type: PlatformType.SWITCH,
    component:SensorSwitchComponent,
    config: InputSwitchConfig,
  },
  {
    type: PlatformType.DS2482,
    component:SensorDs2482Component,
    config: DS18B20busDS2482
  },

  {
    type: PlatformType.DALLAS,
    component:SensorDallasComponent,
    config: DS18B20busFs
  },
  {
    type: PlatformType.LM75,
    component:SensorLm75Component,
    config: Lm75SensorConfig
  },
  {
    type: PlatformType.MODBUS,
    component:SensorModbusComponent,
    config: ModbusSensorConfig
  }
];

export interface SelectableSensorItem {
  type: PlatformType
  component: ComponentType<any>
  config: Object
}
