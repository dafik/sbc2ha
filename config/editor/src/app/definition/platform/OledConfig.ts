import {PlatformConfig} from "./PlatformConfig";
import {Duration} from "../Duration";
import {ScreenType} from "../enums/ScreenType";
import {PlatformType} from "../enums/PlatformType";

export class OledConfig extends PlatformConfig {
  enabled: boolean = false;
  screensaverTimeout: Duration = new Duration("60s");
  screens: ScreenType[]=[];

  public constructor() {
    super(PlatformType.OLED);
  }
}
