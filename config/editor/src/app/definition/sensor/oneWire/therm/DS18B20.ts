import {OneWireTherm} from "./OneWireTherm";
import {PlatformType} from "../../../enums/PlatformType";

export abstract class DS18B20 extends OneWireTherm {

  constructor(platform: PlatformType, name: string, busId: string, address: string) {
    super(platform, name, busId, address);
  }
}
