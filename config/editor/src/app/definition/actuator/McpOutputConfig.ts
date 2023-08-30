import {ActuatorConfig} from "./ActuatorConfig";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";


export class McpOutputConfig extends ActuatorConfig {
    constructor(name: string, outputType: ActuatorType, output: number) {
        super(OutputKindType.MCP, name, outputType, output);
    }
}
