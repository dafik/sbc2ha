import {ThermBusSensorConfig} from "./ThermBusSensorConfig";
import {PlatformType} from "../enums/PlatformType";

export class Lm75SensorConfig extends ThermBusSensorConfig {

  constructor(name: string, busId: string) {
    super(PlatformType.LM75, name, busId);
  }
}
