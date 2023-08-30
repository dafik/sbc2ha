import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {Duration} from "../../../../../../definition/Duration";
import {ActuatorType} from "../../../../../../definition/enums/ActuatorType";
import {GpioOutputConfig} from "../../../../../../definition/actuator/GpioOutputConfig";

@Component({
  selector: 'app-actuator-gpio',
  templateUrl: './actuator-gpio.component.html',
  styleUrls: ['./actuator-gpio.component.scss']
})
export class ActuatorGpioComponent {
  nameCtrl = new FormControl('', Validators.required);
  outputCtrl = new FormControl('1', Validators.required);
  outputTypeCtrl = new FormControl(ActuatorType.SWITCH, Validators.required);
  restoreStateCtrl = new FormControl(true, Validators.required);
  momentaryTurnOnCtrl = new FormControl('');
  momentaryTurnOffCtrl = new FormControl('');
  showInHaCtrl = new FormControl(true, Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    output: this.outputCtrl,
    outputType: this.outputTypeCtrl,
    restoreState: this.restoreStateCtrl,
    momentaryTurnOn: this.momentaryTurnOnCtrl,
    momentaryTurnOff: this.momentaryTurnOffCtrl,
    showInHa: this.showInHaCtrl,

  });
  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<ActuatorGpioComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: GpioOutputConfig },
              public es: ExtensionsService
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.outputCtrl.patchValue(data.config.output.toString());
      this.outputTypeCtrl.patchValue(data.config.outputType);
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
    let config: GpioOutputConfig = new GpioOutputConfig(value.name as string, value.outputType as ActuatorType, Number(value.output))
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
    config.outputType = value.outputType as ActuatorType;
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
  getType() {
    return [ActuatorType.LIGHT,ActuatorType.SWITCH]
  }

    isValid() {
      return this.platform.valid
    }
}
