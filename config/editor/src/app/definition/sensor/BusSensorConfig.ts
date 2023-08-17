import {ScheduledSensorConfig} from "./ScheduledSensorConfig";
import {PlatformType} from "../enums/PlatformType";


export abstract class BusSensorConfig extends ScheduledSensorConfig {
    busId: string;

  constructor(platform: PlatformType, name: string, busId: string) {
    super(platform, name);
    this.busId = busId;
  }
}
