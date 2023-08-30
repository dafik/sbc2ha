import {PlatformConfig} from "../PlatformConfig";
import {PlatformType} from "../../enums/PlatformType";
import {Expose} from "class-transformer";

export abstract class BusConfig extends PlatformConfig {
    @Expose({name: "bus_id"})
    busId: string;

    constructor(platform: PlatformType, busId: string) {
        super(platform);
        this.busId = busId;
    }
}
