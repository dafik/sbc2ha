import {InputConfig} from "./InputConfig";
import {BinarySensorDeviceClassType} from "../../enums/deviceClass/ha/BinarySensorDeviceClassType";
import {InputSensorAction} from "../../enums/InputSensorAction";
import {PlatformType} from "../../enums/PlatformType";
import {ActionConfig} from "../../action/ActionConfig";

export class InputSensorConfig extends InputConfig<ActionsSensor> {
  deviceClass: BinarySensorDeviceClassType = BinarySensorDeviceClassType.NONE;

  constructor(name: string, input: number) {
    super(PlatformType.DIGITAL, name, input);
  }
}
export interface ActionsSensor {
  pressed?: ActionConfig[]
  released?: ActionConfig[]
}
