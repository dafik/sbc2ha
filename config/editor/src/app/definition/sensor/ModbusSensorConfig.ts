import {BusSensorConfig} from "./BusSensorConfig";
import {PlatformType} from "../enums/PlatformType";


export class ModbusSensorConfig extends BusSensorConfig {

    address: number;
    model: string;

    constructor(name: string, busId: string, address: number, model: string) {
        super(PlatformType.MODBUS, name, busId);
        this.address = address;
        this.model = model;
    }
}
