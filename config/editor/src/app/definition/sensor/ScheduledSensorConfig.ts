import {ScalarSensorConfig} from "./ScalarSensorConfig";
import {Duration} from "../Duration";
import {PlatformType} from "../enums/PlatformType";
import {Expose, Transform} from "class-transformer";
import {fromDuration, skipDefault, toDuration} from "../AppConfig";

export abstract class ScheduledSensorConfig extends ScalarSensorConfig {
    @Transform(({value}) => toDuration(value, "60s"), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Transform(({value}) => skipDefault(value, "60s"), {toPlainOnly: true})
    @Expose({name: "update_interval"})
    updateInterval: Duration = new Duration("60s");

    constructor(platform: PlatformType, name: string) {
        super(platform, name);
    }
}
