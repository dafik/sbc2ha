import {I2cBusConfig} from "./I2cBusConfig";
import {PlatformType} from "../../enums/PlatformType";


export class Ds2482BusConfig extends I2cBusConfig {
    public constructor(busId: string, address: number) {
        super(PlatformType.DS2482, busId, address);
    }
}
