import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";
import {Duration} from "../Duration";

export abstract class ActuatorConfig {
    kind: OutputKindType;
    name: string;
    outputType: ActuatorType;
    output: number;
    showInHa: boolean = true;
    restoreState: boolean = false;
    momentaryTurnOn?: Duration;
    momentaryTurnOff?: Duration;


    protected constructor(kind: OutputKindType, name: string, outputType: ActuatorType, output: number) {
        this.kind = kind;
        this.name = name;
        this.outputType = outputType;
        this.output = output;
    }
}
