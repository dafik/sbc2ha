import {ActionConfig} from "./ActionConfig";
import {ActionOutputType} from "../enums/ActionOutputType";
import {ActionType} from "../enums/ActionType";

export class OutputActionConfig extends ActionConfig {
  output: number;
  actionOutput: ActionOutputType;


  constructor(output: number, actionOutput: ActionOutputType) {
    super(ActionType.OUTPUT);
    this.output = output;
    this.actionOutput = actionOutput;
  }
}
