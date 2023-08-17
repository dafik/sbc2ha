import {PlatformType} from "../enums/PlatformType";


export abstract class SensorConfig {
    platform: PlatformType;
    name: string;
    showInHa: boolean = true;

    protected constructor(platform: PlatformType, name: string) {
        this.platform = platform;
        this.name = name;
    }
}
