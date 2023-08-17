import {DS18B20} from "../DS18B20";
import {PlatformType} from "../../../../enums/PlatformType";

export class DS18B20busDS2482 extends DS18B20 {

  constructor(name: string, busId: string, address: string) {
    super(PlatformType.DS2482, name, busId, address);
  }
}
