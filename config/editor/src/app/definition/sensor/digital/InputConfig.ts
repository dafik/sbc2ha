import {SensorConfig} from "../SensorConfig";
import {Duration} from "../../Duration";
import {PlatformType} from "../../enums/PlatformType";
import {Exclude, Expose, Transform, Type} from "class-transformer";
import {fromDuration, skipDefault, toDuration} from "../../AppConfig";

export abstract class InputConfig<A> extends SensorConfig {

    input: number;

    @Transform(({value}) => toDuration(value, "25ms"), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Transform(({value}) => skipDefault(value, "25ms"), {toPlainOnly: true})
    @Expose({name: "bounce_time"})
    bounceTime: Duration = new Duration("25ms");

    @Transform(({value}) => skipDefault(value, false), {toPlainOnly: true})
    inverted: boolean = false;


    @Exclude()
    actionsType: Function;

    @Type(options => {
        return (options?.newObject as InputConfig<A>).actionsType;
    })
    actions?: A;

    protected constructor(platform: PlatformType, name: string, input: number, actionsType: Function) {
        super(platform, name);
        this.input = input;
        this.actionsType = actionsType;
    }
}


