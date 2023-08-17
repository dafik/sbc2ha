import {BusSensorConfig} from "./BusSensorConfig";
import {PlatformType} from "../enums/PlatformType";

export abstract class ThermBusSensorConfig extends BusSensorConfig {
  private unitOfMeasurement: string = "°C";


  constructor(platform: PlatformType, name: string, busId: string) {
    super(platform, name, busId);
  }
}
