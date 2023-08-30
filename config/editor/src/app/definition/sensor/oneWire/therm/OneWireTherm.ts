import {ThermBusSensorConfig} from "../../ThermBusSensorConfig";
import {PlatformType} from "../../../enums/PlatformType";


export abstract class OneWireTherm extends ThermBusSensorConfig {
    address: string;

    constructor(platform: PlatformType, name: string, busId: string, address: string) {
        super(platform, name, busId);
        this.address = address;
    }
}
