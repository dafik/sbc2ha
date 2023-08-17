import {ActuatorConfig} from "./ActuatorConfig";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";


export class GpioOutputConfig extends ActuatorConfig {

  constructor(name: string, outputType: ActuatorType, output: number) {
    super(OutputKindType.GPIO, name, outputType, output);
  }
}
