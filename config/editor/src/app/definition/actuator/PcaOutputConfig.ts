import {ActuatorConfig} from "./ActuatorConfig";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";

export class PcaOutputConfig extends ActuatorConfig {
  percentageDefaultBrightness: number = 1;

  constructor(name: string, outputType: ActuatorType, output: number) {
    super(OutputKindType.PCA, name, outputType, output);
  }
}
