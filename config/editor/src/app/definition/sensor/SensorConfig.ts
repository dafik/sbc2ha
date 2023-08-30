import {PlatformType} from "../enums/PlatformType";
import {Expose, Transform} from "class-transformer";
import {skipDefault} from "../AppConfig";


export abstract class SensorConfig {
    platform: PlatformType;

    name: string;

    @Expose({name: "show_in_ha"})
    @Transform(({value}) => skipDefault(value, true), {toPlainOnly: true})
    showInHa: boolean = true;

    protected constructor(platform: PlatformType, name: string) {
        this.platform = platform;
        this.name = name;
    }
}
