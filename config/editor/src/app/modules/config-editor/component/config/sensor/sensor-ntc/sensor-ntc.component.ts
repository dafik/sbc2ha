import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {ResistanceDirectionType} from "../../../../../../definition/enums/ResistanceDirectionType";
import {Filter} from "../../../../../../definition/sensor/ScalarSensorConfig";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {MatSelectChange} from "@angular/material/select";
import {SensorAnalogComponent} from "../sensor-analog/sensor-analog.component";
import {AnalogSensorConfig} from "../../../../../../definition/sensor/analog/AnalogSensorConfig";
import {
  BconstantCalibration,
  NtcConfig,
  SetPoint,
  ValueCalibration
} from "../../../../../../definition/sensor/analog/NtcConfig";
import {SensorResistanceComponent} from "../sensor-resistance/sensor-resistance.component";
import {ResistanceConfig} from "../../../../../../definition/sensor/analog/ResistanceConfig";

@Component({
  selector: 'app-sensor-ntc',
  templateUrl: './sensor-ntc.component.html',
  styleUrls: ['./sensor-ntc.component.scss']
})
export class SensorNtcComponent {
  nameCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  sensorCtrl = new FormControl('', Validators.required);

  calibrationCtrl = new FormControl('1', Validators.required);

  bConstantCtrl = new FormControl('', Validators.required);
  referenceTemperatureCtrl = new FormControl('', Validators.required);
  referenceResistanceCtrl = new FormControl('', Validators.required);


  hTemperatureCtrl = new FormControl('', Validators.required);
  hResistanceCtrl = new FormControl('', Validators.required);
  mTemperatureCtrl = new FormControl('', Validators.required);
  mResistanceCtrl = new FormControl('', Validators.required);
  lTemperatureCtrl = new FormControl('', Validators.required);
  lResistanceCtrl = new FormControl('', Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    showInHa: this.showInHaCtrl,
    sensor: this.sensorCtrl,

    calibration: this.calibrationCtrl,

    bConstant: this.bConstantCtrl,
    referenceTemperature: this.referenceTemperatureCtrl,
    referenceResistance: this.referenceResistanceCtrl,

    hTemperature: this.hTemperatureCtrl,
    hResistance: this.hResistanceCtrl,
    mTemperature: this.mTemperatureCtrl,
    mResistance: this.mResistanceCtrl,
    lTemperature: this.lTemperatureCtrl,
    lResistance: this.lResistanceCtrl,

  });
  filters: Filter[] = [];

  direction = ResistanceDirectionType;

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorNtcComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: NtcConfig },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.showInHaCtrl.patchValue(data.config.showInHa);
      this.sensorCtrl.patchValue(data.config.sensor);
      if (data.config.bCalibration) {
        this.bConstantCtrl.patchValue(data.config.bCalibration.bConstant.toString());
        this.referenceTemperatureCtrl.patchValue(data.config.bCalibration.referenceTemperature);
        this.referenceResistanceCtrl.patchValue(data.config.bCalibration.referenceResistance);
      }
      if (data.config.vCalibration) {
        this.hTemperatureCtrl.patchValue(data.config.vCalibration.high.temperature);
        this.hResistanceCtrl.patchValue(data.config.vCalibration.high.resistance);
        this.mTemperatureCtrl.patchValue(data.config.vCalibration.mid.temperature);
        this.mResistanceCtrl.patchValue(data.config.vCalibration.mid.resistance);
        this.lTemperatureCtrl.patchValue(data.config.vCalibration.low.temperature);
        this.lResistanceCtrl.patchValue(data.config.vCalibration.low.resistance);
      }
      this.filters = data.config.filters;
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let calibration: ValueCalibration | BconstantCalibration;
    if (value.calibration) {
      calibration = new BconstantCalibration(
        Number(value.bConstant),
        value.referenceTemperature as string,
        value.referenceResistance as string
      )
    } else {
      calibration = new ValueCalibration(
        new SetPoint(value.lTemperature as string, value.lResistance as string),
        new SetPoint(value.mTemperature as string, value.mResistance as string),
        new SetPoint(value.hTemperature as string, value.hResistance as string),
      )
    }

    let config: NtcConfig = new NtcConfig(
      value.name as string,
      value.sensor as string,
      calibration
    )
    config.filters = this.filters;
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: NtcConfig = this.data.config;
    config.name = value.name as string;
    config.sensor = value.sensor as string;
    config.filters = this.filters;
    config.showInHa = value.showInHa as boolean;
    if (this.calibrationCtrl) {
      config.bCalibration = new BconstantCalibration(
        Number(value.bConstant),
        value.referenceTemperature as string,
        value.referenceResistance as string
      )
      config.vCalibration = undefined

    } else {
      config.bCalibration = undefined
      config.vCalibration = new ValueCalibration(
        new SetPoint(value.lTemperature as string, value.lResistance as string),
        new SetPoint(value.mTemperature as string, value.mResistance as string),
        new SetPoint(value.hTemperature as string, value.hResistance as string),
      )
    }


    this.dialogRef.close();
  }

  getSensors() {
    let sensors = this.es.getSensors(PlatformType.RESISTANCE);
    return sensors.sort()
  }

  onSelectSensor($event: MatSelectChange) {
    if ($event.value == "add") {
      $event.source.value = ""

      const dialogRefItem = this.dialog.open(SensorResistanceComponent);

      dialogRefItem.afterClosed().subscribe(result => {
        if (result instanceof ResistanceConfig) {
          this.es.addSensor(result);
        }
        $event.source.open();
      });


    }
  }

    isValid() {
      return this.platform.valid
    }
}
