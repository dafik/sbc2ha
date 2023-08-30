import {ActionType} from "../enums/ActionType";

export abstract class ActionConfig {
    action: ActionType;

    protected constructor(action: ActionType) {
        this.action = action;
    }
}
