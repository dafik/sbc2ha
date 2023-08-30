import {ActionConfig} from "./ActionConfig";
import {ActionOutputType} from "../enums/ActionOutputType";
import {ActionType} from "../enums/ActionType";
import {Expose} from "class-transformer";

export class OutputActionConfig extends ActionConfig {
    output: number;
    @Expose({name: "action_output"})
    actionOutput: ActionOutputType;


    constructor(output: number, actionOutput: ActionOutputType) {
        super(ActionType.OUTPUT);
        this.output = output;
        this.actionOutput = actionOutput;
    }
}
