import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {ActionsSwitch, InputSwitchConfig} from "../../../../../../definition/sensor/digital/InputSwitchConfig";
import {ButtonState} from "../../../../../../definition/enums/ButtonState";
import {SwitchDeviceClassType} from "../../../../../../definition/enums/deviceClass/ha/SwitchDeviceClassType";
import {MatSelectChange} from "@angular/material/select";

@Component({
    selector: 'app-sensor-switch',
    templateUrl: './sensor-switch.component.html',
    styleUrls: ['./sensor-switch.component.scss']
})
export class SensorSwitchComponent {
    nameCtrl = new FormControl('', Validators.required);
    showInHaCtrl = new FormControl(true, Validators.required);
    inputCtrl = new FormControl('1', Validators.required);
    bounceTimeCtrl = new FormControl('25ms', Validators.required);
    deviceClassCtrl = new FormControl(SwitchDeviceClassType.NONE.valueOf(), Validators.required);
    clickDetectionCtrl = new FormControl(ButtonState.SINGLE.valueOf(), Validators.required)


    platform = this._formBuilder.group({
        name: this.nameCtrl,
        showInHa: this.showInHaCtrl,
        input: this.inputCtrl,
        clickDetection: this.clickDetectionCtrl,
        bounceTime: this.bounceTimeCtrl,
        deviceClass: this.deviceClassCtrl
    });
    public actions: ActionsSwitch = {};

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<SensorSwitchComponent>,
                @Inject(MAT_DIALOG_DATA) public data: { config: InputSwitchConfig },
                public es: ExtensionsService,
                public dialog: MatDialog
    ) {
        if (data?.config) {
            this.nameCtrl.patchValue(data.config.name);
            this.showInHaCtrl.patchValue(data.config.showInHa);
            this.inputCtrl.patchValue(data.config.input.toString())
            this.bounceTimeCtrl.patchValue(data.config.bounceTime.duration)
            this.deviceClassCtrl.patchValue(data.config.deviceClass.valueOf())
            this.clickDetectionCtrl.patchValue(data.config.clickDetection)
            if (data.config.actions) {
                this.actions = data.config.actions
            }
        }
    }

    onNoClick() {
        this.dialogRef.close();
    }

    add() {
        let value = this.platform.value;
        let config: InputSwitchConfig = new InputSwitchConfig(
            value.name as string,
            Number(value.input),
        )
        config.showInHa = value.showInHa as boolean;
        config.clickDetection = value.clickDetection as ButtonState
        config.bounceTime.duration = value.bounceTime as string
        config.deviceClass = value.deviceClass as SwitchDeviceClassType
        config.actions = this.actions

        this.dialogRef.close(config);
    }

    edit() {
        let value = this.platform.value;
        let config: InputSwitchConfig = this.data.config;

        config.name = value.name as string;
        config.input = Number(value.input)
        config.showInHa = value.showInHa as boolean;
        config.bounceTime.duration = value.bounceTime as string
        config.deviceClass = value.deviceClass as SwitchDeviceClassType
        config.clickDetection = value.clickDetection as ButtonState

        this.dialogRef.close();
    }

    getDigitalInputs() {
        let digitalInputs = this.es.getDigitalInputs();
        if (this.data?.config) {
            digitalInputs.push(this.data.config.input)
        }
        return digitalInputs.sort((a, b) => a - b)
    }

    protected readonly SwitchDeviceClassType = SwitchDeviceClassType;

    getClickDetection() {
        return [ButtonState.SINGLE, ButtonState.DOUBLE, ButtonState.LONG]
    }

    getActionsType(value: string | null) {
        if (value == ButtonState.SINGLE) {
            return [ButtonState.SINGLE];
        }
        if (value == ButtonState.DOUBLE) {
            return [ButtonState.SINGLE, ButtonState.DOUBLE];
        }
        if (value == ButtonState.LONG) {
            return [ButtonState.SINGLE, ButtonState.DOUBLE, ButtonState.LONG];
        }

        return []
    }

    changeClickDetection($event: MatSelectChange) {
        let allowed = this.getActionsType($event.value);
        Object.keys(this.actions).forEach(value => {
            if (!allowed.includes(<ButtonState>value)) {
                delete (this.actions as any)[value];
            }
        })
    }

    isValid() {
        return this.platform.valid;
    }
}
