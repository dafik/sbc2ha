import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Filter} from "../../../../../../definition/sensor/ScalarSensorConfig";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {DS18B20busDS2482} from "../../../../../../definition/sensor/oneWire/therm/ds2482/DS18B20busDS2482";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {MatSelectChange} from "@angular/material/select";
import {PlatformI2cComponent} from "../../platform/platform-i2c/platform-i2c.component";
import {Ds2482BusConfig} from "../../../../../../definition/platform/bus/Ds2482BusConfig";

@Component({
  selector: 'app-sensor-ds2482',
  templateUrl: './sensor-ds2482.component.html',
  styleUrls: ['./sensor-ds2482.component.scss']
})
export class SensorDs2482Component {

  nameCtrl = new FormControl('', Validators.required);
  addressCtrl = new FormControl('', Validators.required);
  busIdCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  updateIntervalCtrl = new FormControl('60s', Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    address: this.addressCtrl,
    busId: this.busIdCtrl,
    showInHa: this.showInHaCtrl,
    updateInterval: this.updateIntervalCtrl
  });
  filters: Filter[] = [];

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorDs2482Component>,
              @Inject(MAT_DIALOG_DATA) public data: { config: DS18B20busDS2482 },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.addressCtrl.patchValue(data.config.address);
      this.busIdCtrl.patchValue(data.config.busId);
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
    let config: DS18B20busDS2482 = new DS18B20busDS2482(value.name as string, value.busId as string, value.address as string)
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: DS18B20busDS2482 = this.data.config;
    config.name = value.name as string;
    config.busId = value.busId as string;
    config.address = value.address as string;
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close();
  }

  getBuses() {
    let buses = this.es.getBuses(PlatformType.DS2482);
    return buses.sort()
  }


  onSelectSensor($event: MatSelectChange) {
    if ($event.value == "add") {
      $event.source.value = ""

      const dialogRefItem = this.dialog.open(PlatformI2cComponent, {data: {platformType: PlatformType.DS2482}});

      dialogRefItem.afterClosed().subscribe(result => {
        if (result instanceof Ds2482BusConfig) {
          this.es.addPlatform(result);
        }
        $event.source.open();
      });


    }
  }

    isValid() {
      return this.platform.valid
    }
}

