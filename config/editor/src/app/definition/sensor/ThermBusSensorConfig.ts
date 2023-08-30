import {BusSensorConfig} from "./BusSensorConfig";
import {PlatformType} from "../enums/PlatformType";
import {Expose, Transform} from "class-transformer";
import {skipDefault} from "../AppConfig";

export abstract class ThermBusSensorConfig extends BusSensorConfig {
    @Expose({name: "unit_of_measurement"})
    @Transform(({value}) => skipDefault(value, "°C"), {toPlainOnly: true})
    private unitOfMeasurement: string = "°C";


    constructor(platform: PlatformType, name: string, busId: string) {
        super(platform, name, busId);
    }
}
