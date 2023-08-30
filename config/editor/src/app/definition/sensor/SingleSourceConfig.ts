import {ScalarSensorConfig} from "./ScalarSensorConfig";
import {PlatformType} from "../enums/PlatformType";


export abstract class SingleSourceConfig extends ScalarSensorConfig {
    sensor: string;

    protected constructor(platform: PlatformType, name: string, sensor: string) {
        super(platform, name);
        this.sensor = sensor;
    }
}
