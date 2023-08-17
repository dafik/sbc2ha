import {InputConfig} from "./InputConfig";
import {ButtonState} from "../../enums/ButtonState";
import {SwitchDeviceClassType} from "../../enums/deviceClass/ha/SwitchDeviceClassType";
import {InputSwitchAction} from "../../enums/InputSwitchAction";
import {PlatformType} from "../../enums/PlatformType";
import {ActionConfig} from "../../action/ActionConfig";

export class InputSwitchConfig extends InputConfig<ActionsSwitch> {
  clickDetection: ButtonState = ButtonState.SINGLE;
  deviceClass: SwitchDeviceClassType = SwitchDeviceClassType.NONE;

  constructor(name: string, input: number) {
    super(PlatformType.SWITCH, name, input);
  }
}

export interface ActionsSwitch {
  single?: ActionConfig[]
  double?: ActionConfig[]
  long?: ActionConfig[]
}
