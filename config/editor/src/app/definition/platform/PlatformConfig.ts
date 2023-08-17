import {PlatformType} from "../enums/PlatformType";

export abstract class PlatformConfig {
   public platform: PlatformType;

    protected constructor(platform: PlatformType) {
        this.platform = platform;
    }
}
