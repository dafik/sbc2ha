import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ActionType} from "../../../../../../../definition/enums/ActionType";
import {ActionMqttComponent} from "../action-mqtt/action-mqtt.component";
import {ActionOutputComponent} from "../action-output/action-output.component";
import {ActionCoverComponent} from "../action-cover/action-cover.component";
import {ActionConfig} from "../../../../../../../definition/action/ActionConfig";

@Component({
    selector: 'app-actions-add',
    templateUrl: './actions-add.component.html',
    styleUrls: ['./actions-add.component.scss']
})
export class ActionsAddComponent {

    constructor(
        public dialogRef: MatDialogRef<ActionsAddComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { actions: ActionConfig[] },
        public dialog: MatDialog
    ) {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }


    addAction(item: ActionType) {
        let component;
        switch (item) {
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

        const dialogRef = this.dialog.open<ActionMqttComponent | ActionOutputComponent | ActionCoverComponent, any, ActionConfig>(component);

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.data.actions.push(result)
            }
            this.dialogRef.close()
        });


    }

    protected readonly ActionType = ActionType;
}
