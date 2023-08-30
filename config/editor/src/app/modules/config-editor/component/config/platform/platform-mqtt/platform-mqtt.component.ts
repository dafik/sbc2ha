import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MqttConfig} from "../../../../../../definition/platform/MqttConfig";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
    selector: 'app-platform-mqtt',
    templateUrl: './platform-mqtt.component.html',
    styleUrls: ['./platform-mqtt.component.scss']
})
export class PlatformMqttComponent {
    public readonly HOMEASSISTANT = 'homeassistant';
    public readonly BONEIO = 'boneio';
    public readonly PORT = 1883

    hostCtrl = new FormControl('', [Validators.required]);
    usernameCtrl = new FormControl('', Validators.required);
    passwordCtrl = new FormControl('', Validators.required);
    portCtrl = new FormControl<null | number>(this.PORT);
    topicPrefixCtrl = new FormControl(this.BONEIO);
    haDiscoveryEnabledCtrl = new FormControl(true);
    haDiscoveryTopicPrefixCtrl = new FormControl(this.HOMEASSISTANT);

    platform = this._formBuilder.group({
        host: this.hostCtrl,
        username: this.usernameCtrl,
        password: this.passwordCtrl,
        port: this.portCtrl,
        topicPrefix: this.topicPrefixCtrl,
        haDiscoveryEnabled: this.haDiscoveryEnabledCtrl,
        haDiscoveryTopicPrefix: this.haDiscoveryTopicPrefixCtrl
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<PlatformMqttComponent>,
                @Inject(MAT_DIALOG_DATA) public data: { config: MqttConfig },) {
        if (data?.config) {
            this.hostCtrl.patchValue(data.config.host);
            this.usernameCtrl.patchValue(data.config.host);
            this.passwordCtrl.patchValue(data.config.password);
            if (data.config.port) {
                this.portCtrl.patchValue(data.config.port);
            }
            this.topicPrefixCtrl.patchValue(data.config.topicPrefix);
            this.haDiscoveryEnabledCtrl.patchValue(data.config.haDiscovery.enabled);
            this.haDiscoveryTopicPrefixCtrl.patchValue(data.config.haDiscovery.topicPrefix);
        }

    }

    onNoClick() {
        this.dialogRef.close();
    }

    add() {
        let value = this.platform.value;
        let config: MqttConfig = new MqttConfig(value.host as string, value.username as string, value.password as string)
        config.port = value.port as number;
        config.topicPrefix = value.topicPrefix as string;
        config.haDiscovery.enabled = value.haDiscoveryEnabled as boolean;
        config.haDiscovery.topicPrefix = value.haDiscoveryTopicPrefix as string;

        this.dialogRef.close(config);
    }

    edit() {
        let value = this.platform.value;
        let config: MqttConfig = this.data.config;
        config.host = value.host as string;
        config.username = value.username as string;
        config.password = value.password as string;
        config.port = value.port as number;
        config.topicPrefix = value.topicPrefix as string;
        config.haDiscovery.enabled = value.haDiscoveryEnabled as boolean;
        config.haDiscovery.topicPrefix = value.haDiscoveryTopicPrefix as string;

        this.dialogRef.close();
    }

    isValid() {
        return this.platform.valid
    }
}
