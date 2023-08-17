import {PlatformConfig} from "../PlatformConfig";
import {PlatformType} from "../../enums/PlatformType";
export abstract class BusConfig extends PlatformConfig {
    busId: string;


  constructor(platform: PlatformType, busId: string) {
    super(platform);
    this.busId = busId;
  }
}
