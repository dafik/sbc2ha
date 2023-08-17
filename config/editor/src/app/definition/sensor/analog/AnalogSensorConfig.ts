import {ScheduledSensorConfig} from "../ScheduledSensorConfig";
import {PlatformType} from "../../enums/PlatformType";

export class AnalogSensorConfig extends ScheduledSensorConfig {
  analog: number;

  constructor(name: string, analog: number) {
    super(PlatformType.ANALOG, name);
    this.analog = analog;
  }
}
