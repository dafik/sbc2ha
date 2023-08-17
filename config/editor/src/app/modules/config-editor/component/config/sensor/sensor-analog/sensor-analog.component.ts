import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {AnalogSensorConfig} from "../../../../../../definition/sensor/analog/AnalogSensorConfig";
import {Filter} from "../../../../../../definition/sensor/ScalarSensorConfig";
import {ExtensionsService} from "../../../../extensions.service";
import {ValueFilterType} from "../../../../../../definition/enums/ValueFilterType";

@Component({
  selector: 'app-sensor-analog',
  templateUrl: './sensor-analog.component.html',
  styleUrls: ['./sensor-analog.component.scss']
})
export class SensorAnalogComponent {

  nameCtrl = new FormControl('', Validators.required);
  analogCtrl = new FormControl('1', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  updateIntervalCtrl = new FormControl('60s', Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    analog: this.analogCtrl,
    showInHa: this.showInHaCtrl,
    updateInterval: this.updateIntervalCtrl
  });
  filters: Filter[] = [];

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorAnalogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: AnalogSensorConfig },
              public es: ExtensionsService
              ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.analogCtrl.patchValue(data.config.analog.toString());
      this.showInHaCtrl.patchValue(data.config.showInHa);
      this.updateIntervalCtrl.patchValue(data.config.updateInterval.duration)
      this.filters = data.config.filters;
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: AnalogSensorConfig = new AnalogSensorConfig(value.name as string, Number(value.analog) )
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: AnalogSensorConfig = this.data.config;
    config.name = value.name as string;
    config.analog = Number(value.analog);
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close();
  }

  protected readonly ValueFilterType = ValueFilterType;

  getAnalogInputs() {
    let analogInputs = this.es.getAnalogInputs();
    if (this.data?.config){
      analogInputs.push(this.data.config.analog)
    }
    return analogInputs.sort()
  }
}
