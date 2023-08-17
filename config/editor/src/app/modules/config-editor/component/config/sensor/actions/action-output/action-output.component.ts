import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {OutputActionConfig} from "../../../../../../../definition/action/OutputActionConfig";
import {ExtensionsService} from "../../../../../extensions.service";
import {ActionOutputType} from "../../../../../../../definition/enums/ActionOutputType";
import {MatSelectChange} from "@angular/material/select";
import {SensorAnalogComponent} from "../../sensor-analog/sensor-analog.component";
import {AnalogSensorConfig} from "../../../../../../../definition/sensor/analog/AnalogSensorConfig";
import {ActuatorAddComponent} from "../../../../actuator/actuator-add/actuator-add.component";
import {ActuatorConfig} from "../../../../../../../definition/actuator/ActuatorConfig";

@Component({
    selector: 'app-action-output',
    templateUrl: './action-output.component.html',
    styleUrls: ['./action-output.component.scss']
})
export class ActionOutputComponent {

    outputCtrl = new FormControl(1, Validators.required);
    actionOutputCtrl = new FormControl(ActionOutputType.TOGGLE, Validators.required);
    platform = this._formBuilder.group({
        output: this.outputCtrl,
        actionOutput: this.actionOutputCtrl
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<ActionOutputComponent>,
                @Inject(MAT_DIALOG_DATA) public data: { config: OutputActionConfig },
                private es: ExtensionsService,
                public dialog: MatDialog
    ) {
        if (data?.config) {
            this.outputCtrl.patchValue(data.config.output);
            this.actionOutputCtrl.patchValue(data.config.actionOutput)
        }
    }

    onNoClick() {
        this.dialogRef.close();
    }

    getOutputs() {
        return this.es.getActuators()
    }

    add() {
        let value = this.platform.value;
        let config: OutputActionConfig = new OutputActionConfig(value.output as number, value.actionOutput as ActionOutputType);
        this.dialogRef.close(config);
    }

    edit() {
        let value = this.platform.value;
        let config: OutputActionConfig = this.data.config;

        config.output = value.output as number;
        config.actionOutput = value.actionOutput as ActionOutputType;

        this.dialogRef.close();
    }

    protected readonly ActionOutputType = ActionOutputType;

    onSelectOutput($event: MatSelectChange) {
        if ($event.value == "add") {
            $event.source.value = ""

            const dialogRefItem = this.dialog.open(ActuatorAddComponent);

            dialogRefItem.afterClosed().subscribe(result => {
                if (result instanceof ActuatorConfig) {
                    this.es.addActuator(result);
                }
                $event.source.open();
            });


        }
    }
}
