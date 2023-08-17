import {I2cBusConfig} from "./I2cBusConfig";
import {PlatformType} from "../../enums/PlatformType";

export class Lm75BusConfig extends I2cBusConfig {
    public constructor(busId: string, address: number) {
        super(PlatformType.LM75,busId,address);
    }
}
