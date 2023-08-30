import {PlatformType} from "../../enums/PlatformType";
import {I2cBusConfig} from "./I2cBusConfig";

export class Mcp23017BusConfig extends I2cBusConfig {
    public constructor(busId: string, address: number) {
        super(PlatformType.MCP23017, busId, address);
    }
}
