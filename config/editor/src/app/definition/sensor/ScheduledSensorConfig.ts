import {ScalarSensorConfig} from "./ScalarSensorConfig";
import {Duration} from "../Duration";
import {PlatformType} from "../enums/PlatformType";

export abstract class ScheduledSensorConfig extends ScalarSensorConfig {
  updateInterval: Duration = new Duration("60s");

  constructor(platform: PlatformType, name: string) {
    super(platform, name);
  }
}
