import {BusConfig} from "./BusConfig";
import {PlatformType} from "../../enums/PlatformType";

export class DallasBusConfig extends BusConfig {
  public constructor(busId: string) {
    super(PlatformType.DALLAS, busId);
  }
}
