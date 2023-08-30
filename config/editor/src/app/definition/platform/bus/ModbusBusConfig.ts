import {BusConfig} from "./BusConfig";
import {UartType} from "../../enums/UartType";
import {PlatformType} from "../../enums/PlatformType";


export class ModbusBusConfig extends BusConfig {
    private static BUS_ID: string = "modbus";
    uart: UartType

    public constructor(uart: UartType) {
        super(PlatformType.MODBUS, ModbusBusConfig.BUS_ID);
        this.uart = uart;
    }
}
