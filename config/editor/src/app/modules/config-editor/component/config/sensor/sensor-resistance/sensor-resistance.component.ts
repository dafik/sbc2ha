import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Filter} from "../../../../../../definition/sensor/ScalarSensorConfig";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {ResistanceConfig} from "../../../../../../definition/sensor/analog/ResistanceConfig";
import {ResistanceDirectionType} from "../../../../../../definition/enums/ResistanceDirectionType";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {MatSelectChange} from "@angular/material/select";
import {SensorConfig} from "../../../../../../definition/sensor/SensorConfig";
import {SensorAnalogComponent} from "../sensor-analog/sensor-analog.component";
import {AnalogSensorConfig} from "../../../../../../definition/sensor/analog/AnalogSensorConfig";

@Component({
  selector: 'app-sensor-resistance',
  templateUrl: './sensor-resistance.component.html',
  styleUrls: ['./sensor-resistance.component.scss']
})
export class SensorResistanceComponent {

  nameCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  sensorCtrl = new FormControl('1', Validators.required);
  directionCtrl = new FormControl(ResistanceDirectionType.DOWNSTREAM, Validators.required);
  resistorCtrl = new FormControl('1', Validators.required);
  referenceVoltageCtrl = new FormControl('1', Validators.required);

  platform = this._formBuilder.group({
    name: this.nameCtrl,
    showInHa: this.showInHaCtrl,
    sensor: this.sensorCtrl,
    direction: this.directionCtrl,
    resistor: this.resistorCtrl,
    referenceVoltage: this.referenceVoltageCtrl
  });
  filters: Filter[] = [];

  direction = ResistanceDirectionType;

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorResistanceComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: ResistanceConfig },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.showInHaCtrl.patchValue(data.config.showInHa);
      this.sensorCtrl.patchValue(data.config.sensor);
      this.directionCtrl.patchValue(data.config.direction);
      this.resistorCtrl.patchValue(data.config.resistor);
      this.referenceVoltageCtrl.patchValue(data.config.referenceVoltage);

      this.filters = data.config.filters;
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: ResistanceConfig = new ResistanceConfig(
      value.name as string,
      value.sensor as string,
      value.direction as ResistanceDirectionType,
      value.resistor as string,
      value.referenceVoltage as string
    )
    config.filters = this.filters;
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: ResistanceConfig = this.data.config;
    config.name = value.name as string;
    config.sensor = value.sensor as string;
    config.direction = value.direction as ResistanceDirectionType;
    config.resistor = value.resistor as string;
    config.referenceVoltage = value.referenceVoltage as string;
    config.filters = this.filters;
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close();
  }

  getSensors() {
    let sensors = this.es.getSensors(PlatformType.ANALOG);
    return sensors.sort()
  }

  onSelectSensor($event: MatSelectChange) {
    if ($event.value == "add") {
      $event.source.value = ""

      const dialogRefItem = this.dialog.open(SensorAnalogComponent);

      dialogRefItem.afterClosed().subscribe(result => {
        if (result instanceof AnalogSensorConfig) {
            this.es.addSensor(result);
        }
        $event.source.open();
      });


    }
  }

}
