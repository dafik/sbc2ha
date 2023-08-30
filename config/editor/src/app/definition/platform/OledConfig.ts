import {PlatformConfig} from "./PlatformConfig";
import {Duration} from "../Duration";
import {ScreenType} from "../enums/ScreenType";
import {PlatformType} from "../enums/PlatformType";
import {Expose, Transform} from "class-transformer";
import {fromDuration, skipDefault, toDuration} from "../AppConfig";

export class OledConfig extends PlatformConfig {
    @Transform(({value}) => skipDefault(value, false), {toPlainOnly: true})
    enabled: boolean = false;
    @Transform(({value}) => toDuration(value, "60s"), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Transform(({value}) => skipDefault(value, "60s"), {toPlainOnly: true})
    @Expose({name: "screensaver_timeout"})
    screensaverTimeout: Duration = new Duration("60s");
    @Transform(({value}) => skipDefault(value, []), {toPlainOnly: true})
    screens: ScreenType[] = [];

    public constructor() {
        super(PlatformType.OLED);
    }
}
