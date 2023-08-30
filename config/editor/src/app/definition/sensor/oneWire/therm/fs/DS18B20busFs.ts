import {DS18B20} from "../DS18B20";
import {PlatformType} from "../../../../enums/PlatformType";

export class DS18B20busFs extends DS18B20 {

    constructor(name: string, busId: string, address: string) {
        super(PlatformType.DALLAS, name, busId, address);
    }
}
