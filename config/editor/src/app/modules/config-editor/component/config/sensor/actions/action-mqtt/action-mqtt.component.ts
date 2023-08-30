import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MqttActionConfig} from "../../../../../../../definition/action/MqttActionConfig";

@Component({
    selector: 'app-action-mqtt',
    templateUrl: './action-mqtt.component.html',
    styleUrls: ['./action-mqtt.component.scss']
})
export class ActionMqttComponent {

    topicCtrl = new FormControl('', Validators.required);
    actionMqttMsgCtrl = new FormControl('', Validators.required);
    platform = this._formBuilder.group({
        topic: this.topicCtrl,
        actionMqttMsg: this.actionMqttMsgCtrl
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<ActionMqttComponent>,
                @Inject(MAT_DIALOG_DATA) public data: { config: MqttActionConfig },) {
        if (data?.config) {
            this.topicCtrl.patchValue(data.config.topic);
            this.actionMqttMsgCtrl.patchValue(data.config.actionMqttMsg)
        }
    }

    onNoClick() {
        this.dialogRef.close();
    }

    add() {
        let value = this.platform.value;
        let config: MqttActionConfig = new MqttActionConfig(value.topic as string, value.actionMqttMsg as string);
        this.dialogRef.close(config);
    }

    edit() {
        let value = this.platform.value;
        let config: MqttActionConfig = this.data.config;

        config.topic = value.topic as string;
        config.actionMqttMsg = value.actionMqttMsg as string;

        this.dialogRef.close();
    }

    isValid() {
        return this.platform.valid
    }
}
