import {PlatformType} from "../../enums/PlatformType";
import {I2cBusConfig} from "./I2cBusConfig";

export class PCA9685BusConfig extends I2cBusConfig {
  public constructor(busId: string, address: number) {
    super(PlatformType.PCA9685, busId, address);
  }
}
