import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";
import {Duration} from "../Duration";
import {Expose, Transform} from "class-transformer";
import {fromDuration, skipDefault, toDuration} from "../AppConfig";

export abstract class ActuatorConfig {
    kind: OutputKindType;

    name: string;

    @Expose({name: "output_type"})
    outputType: ActuatorType;

    output: number;

    @Expose({name: "show_in_ha"})
    @Transform(({value}) => skipDefault(value, true), {toPlainOnly: true})
    showInHa: boolean = true;

    @Expose({name: "restore_state"})
    @Transform(({value}) => skipDefault(value, false), {toPlainOnly: true})
    restoreState: boolean = false;

    @Transform(({value}) => toDuration(value), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Expose({name: "momentary_turn_on"})
    momentaryTurnOn?: Duration;

    @Transform(({value}) => toDuration(value), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Expose({name: "momentary_turn_off"})
    momentaryTurnOff?: Duration;


    protected constructor(kind: OutputKindType, name: string, outputType: ActuatorType, output: number) {
        this.kind = kind;
        this.name = name;
        this.outputType = outputType;
        this.output = output;
    }
}
