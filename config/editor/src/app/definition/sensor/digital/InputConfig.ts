import {SensorConfig} from "../SensorConfig";
import {Duration} from "../../Duration";
import {PlatformType} from "../../enums/PlatformType";

export abstract class InputConfig<A> extends SensorConfig {

    input: number;
    bounceTime: Duration = new Duration("25ms");
    inverted: boolean = false;
    actions?: A;

    protected constructor(platform: PlatformType, name: string, input: number) {
        super(platform, name);
        this.input = input;
    }
}


