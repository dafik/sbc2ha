import {Component, Input} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {FilterAddComponent} from "../../../filter/filter-add/filter-add.component";
import {ActionsSwitch} from "../../../../../../../../definition/sensor/digital/InputSwitchConfig";
import {ActionConfig} from "../../../../../../../../definition/action/ActionConfig";
import {ActionsAddTypeComponent} from "../../actions-add-type/actions-add-type.component";
import {ActionsAddComponent} from "../../actions-add/actions-add.component";
import {ActionMqttComponent} from "../../action-mqtt/action-mqtt.component";
import {ActionOutputComponent} from "../../action-output/action-output.component";
import {ActionCoverComponent} from "../../action-cover/action-cover.component";
import {ActionType} from "../../../../../../../../definition/enums/ActionType";

@Component({
    selector: 'app-actions-switch-list',
    templateUrl: './actions-switch-list.component.html',
    styleUrls: ['./actions-switch-list.component.scss']
})
export class ActionsSwitchListComponent {
    @Input({required: true}) actions!: ActionsSwitch;
    @Input({required: true}) actionsType!: string[];

    constructor(public dialog: MatDialog) {
    }

    addActionType() {
        const dialogRef = this.dialog.open(ActionsAddTypeComponent, {data: {types: this.actionsType.filter(value => !this.getActionTypes(this.actions).includes(value))}});

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                (this.actions as any)[result] = [];
            }
        });
    }

    edit(trigger: string) {
        const dialogRef = this.dialog.open(FilterAddComponent, {
            data: {key: trigger, value: (this.actions as any)[trigger] as ActionConfig[]},
        });
    }

    delete(trigger: string) {
        delete (this.actions as any)[trigger];
    }

    protected readonly Object = Object;

    getAction(trigger: string) {
        if (trigger == "single") {
            return this.actions.single as ActionConfig[]
        } else if (trigger == "double") {
            return this.actions.double as ActionConfig[]
        } else {
            return this.actions.long as ActionConfig[]
        }
    }

    getActionTypes(actions: ActionsSwitch) {
        return Object.keys(actions)
    }


    add(trigger: string) {

    }

    addAction(trigger: string) {


        const dialogRef = this.dialog.open<ActionsAddComponent, any, ActionConfig>(ActionsAddComponent,
            {
                data: {
                    actions: (this.actions as any)[trigger] as ActionConfig[]
                }
            });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                ((this.actions as any)[trigger] as ActionConfig[]).push(result)
            }
        });

    }

    editAction(config: ActionConfig) {
        let component;
        switch (config.action) {
            case ActionType.MQTT:
                component = ActionMqttComponent
                break;
            case ActionType.OUTPUT:
                component = ActionOutputComponent
                break;
            case ActionType.COVER:
                component = ActionCoverComponent
                break;
        }

        this.dialog.open<ActionMqttComponent | ActionOutputComponent | ActionCoverComponent, any, ActionConfig>(component, {data: {config: config}});
    }

    deleteAction(trigger: string, i: number) {
        ((this.actions as any)[trigger] as ActionConfig[]).splice(i, 1)
    }
}


