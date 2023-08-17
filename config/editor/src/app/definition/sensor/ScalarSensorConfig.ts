import {ValueFilterType} from "../enums/ValueFilterType";
import {SensorConfig} from "./SensorConfig";
import {PlatformType} from "../enums/PlatformType";

export abstract class ScalarSensorConfig extends SensorConfig {
  filters: Filter[] = [];

  constructor(platform: PlatformType, name: string) {
    super(platform, name);
  }
}

export class Filter {
  type: ValueFilterType;
  value: number;

  constructor(type: ValueFilterType, value: number) {
    this.type = type;
    this.value = value;
  }
}
