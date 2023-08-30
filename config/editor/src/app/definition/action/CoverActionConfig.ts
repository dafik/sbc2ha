import {ActionConfig} from "./ActionConfig";
import {ActionCoverType} from "../enums/ActionCoverType";
import {ActionType} from "../enums/ActionType";
import {Expose} from "class-transformer";

export class CoverActionConfig extends ActionConfig {
    output: number;
    @Expose({name: "action_cover"})
    actionCover: ActionCoverType;


    constructor(output: number, actionCover: ActionCoverType) {
        super(ActionType.COVER);
        this.output = output;
        this.actionCover = actionCover;
    }
}
