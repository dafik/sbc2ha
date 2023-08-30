import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {InputSensorConfig} from "../../../../../../definition/sensor/digital/InputSensorConfig";
import {BinarySensorDeviceClassType} from "../../../../../../definition/enums/deviceClass/ha/BinarySensorDeviceClassType";
import {zero} from "../../../../../../shared/zero";

@Component({
  selector: 'app-sensor-digital',
  templateUrl: './sensor-digital.component.html',
  styleUrls: ['./sensor-digital.component.scss']
})
export class SensorDigitalComponent {
  nameCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  inputCtrl = new FormControl('1', Validators.required);
  bounceTimeCtrl = new FormControl('25ms', Validators.required);
  invertedCtrl = new FormControl(false, Validators.required);
  deviceClassCtrl = new FormControl(BinarySensorDeviceClassType.NONE, Validators.required);

  platform = this._formBuilder.group({
    name: this.nameCtrl,
    showInHa: this.showInHaCtrl,
    input: this.inputCtrl,
    bounceTime: this.bounceTimeCtrl,
    inverted: this.invertedCtrl,
    deviceClass: this.deviceClassCtrl
  });

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorDigitalComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: InputSensorConfig },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.showInHaCtrl.patchValue(data.config.showInHa);
      this.inputCtrl.patchValue(data.config.input.toString())
      this.bounceTimeCtrl.patchValue(data.config.bounceTime.duration)
      this.invertedCtrl.patchValue(data.config.inverted)
      this.deviceClassCtrl.patchValue(data.config.deviceClass)
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: InputSensorConfig = new InputSensorConfig(
      value.name as string,
      Number(value.input),
    )
    config.showInHa = value.showInHa as boolean;
    config.bounceTime.duration = value.bounceTime as string
    config.inverted = value.inverted as boolean
    config.deviceClass = value.deviceClass as BinarySensorDeviceClassType

    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: InputSensorConfig = this.data.config;

    config.name = value.name as string;
    config.input = Number(value.input)
    config.showInHa = value.showInHa as boolean;
    config.bounceTime.duration = value.bounceTime as string
    config.inverted = value.inverted as boolean
    config.deviceClass = value.deviceClass as BinarySensorDeviceClassType

    this.dialogRef.close();
  }

  getDigitalInputs() {
    let digitalInputs = this.es.getDigitalInputs();
    if (this.data?.config) {
      digitalInputs.push(this.data.config.input)
    }
    return digitalInputs.sort((a, b) => a - b)
  }

  protected readonly BinarySensorDeviceClassType = BinarySensorDeviceClassType;
    protected readonly zero = zero;

    isValid() {
      return this.platform.valid
    }
}
