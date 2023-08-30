import {BusConfig} from "./BusConfig";
import {PlatformType} from "../../enums/PlatformType";


export class I2cBusConfig extends BusConfig {
    address: number;

    constructor(platform: PlatformType, busId: string, address: number) {
        super(platform, busId);
        this.address = address;
    }
}
