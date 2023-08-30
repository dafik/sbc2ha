import {ScheduledSensorConfig} from "./ScheduledSensorConfig";
import {PlatformType} from "../enums/PlatformType";
import {Expose} from "class-transformer";


export abstract class BusSensorConfig extends ScheduledSensorConfig {
    @Expose({name: "bus_id"})
    busId: string;

    constructor(platform: PlatformType, name: string, busId: string) {
        super(platform, name);
        this.busId = busId;
    }
}
