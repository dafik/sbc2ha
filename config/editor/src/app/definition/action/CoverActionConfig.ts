import {ActionConfig} from "./ActionConfig";
import {ActionCoverType} from "../enums/ActionCoverType";
import {ActionType} from "../enums/ActionType";

export class CoverActionConfig extends ActionConfig {
  output: number;
  actionCover: ActionCoverType;


  constructor(output: number, actionCover: ActionCoverType) {
    super(ActionType.COVER);
    this.output = output;
    this.actionCover = actionCover;
  }
}
