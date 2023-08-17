import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {ActuatorType} from "../../../../../../definition/enums/ActuatorType";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {GpioOutputConfig} from "../../../../../../definition/actuator/GpioOutputConfig";
import {ExtensionsService} from "../../../../extensions.service";
import {Duration} from "../../../../../../definition/Duration";

@Component({
  selector: 'app-actuator-gpiopwm',
  templateUrl: './actuator-gpiopwm.component.html',
  styleUrls: ['./actuator-gpiopwm.component.scss']
})
export class ActuatorGpiopwmComponent {
  nameCtrl = new FormControl('', Validators.required);
  outputCtrl = new FormControl('1', Validators.required);
  restoreStateCtrl = new FormControl(true, Validators.required);
  momentaryTurnOnCtrl = new FormControl('');
  momentaryTurnOffCtrl = new FormControl('');
  showInHaCtrl = new FormControl(true, Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    output: this.outputCtrl,
      restoreState: this.restoreStateCtrl,
    momentaryTurnOn: this.momentaryTurnOnCtrl,
    momentaryTurnOff: this.momentaryTurnOffCtrl,
    showInHa: this.showInHaCtrl,

  });

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<ActuatorGpiopwmComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: GpioOutputConfig },
              public es: ExtensionsService
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.outputCtrl.patchValue(data.config.output.toString());
        this.restoreStateCtrl.patchValue(data.config.restoreState);
      this.momentaryTurnOnCtrl.patchValue(data.config.momentaryTurnOn?.duration as string);
      this.momentaryTurnOffCtrl.patchValue(data.config.momentaryTurnOff?.duration as string);
      this.showInHaCtrl.patchValue(data.config.showInHa);
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: GpioOutputConfig = new GpioOutputConfig(value.name as string, ActuatorType.LED, Number(value.output))
    config.restoreState = value.restoreState as boolean
    if (value.momentaryTurnOn) {
      config.momentaryTurnOn = new Duration(value.momentaryTurnOn as string)
    }
    if (value.momentaryTurnOff) {
      config.momentaryTurnOff = new Duration(value.momentaryTurnOff as string)
    }
    config.showInHa = value.showInHa as boolean;
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: GpioOutputConfig = this.data.config;
    config.name = value.name as string;
    config.outputType = ActuatorType.LED;
    config.output = Number(value.output);
    config.restoreState = value.restoreState as boolean
    if (value.momentaryTurnOn) {
      config.momentaryTurnOn = new Duration(value.momentaryTurnOn as string)
    } else {
      config.momentaryTurnOn = undefined
    }
    if (value.momentaryTurnOff) {
      config.momentaryTurnOff = new Duration(value.momentaryTurnOff as string)
    } else {
      config.momentaryTurnOff = undefined
    }
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close();
  }

  getOutput() {
    let outputs = this.es.getOutputs();
    if (this.data?.config) {
      outputs.push(this.data.config.output)
    }
    return outputs.sort((a, b) => a - b)
  }

}
