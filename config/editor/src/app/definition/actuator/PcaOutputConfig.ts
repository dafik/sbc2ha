import {ActuatorConfig} from "./ActuatorConfig";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";
import {Expose, Transform} from "class-transformer";
import {skipDefault} from "../AppConfig";

export class PcaOutputConfig extends ActuatorConfig {
    @Expose({name: "percentage_default_brightness"})
    @Transform(({value}) => skipDefault(value, 1), {toPlainOnly: true})
    percentageDefaultBrightness: number = 1;

    constructor(name: string, outputType: ActuatorType, output: number) {
        super(OutputKindType.PCA, name, outputType, output);
    }
}
