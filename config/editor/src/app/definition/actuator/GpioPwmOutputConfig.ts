import {ActuatorConfig} from "./ActuatorConfig";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";


export class GpioPwmOutputConfig extends ActuatorConfig {

    constructor(name: string, outputType: ActuatorType, output: number) {
        super(OutputKindType.GPIOPWM, name, outputType, output);
    }
}
