import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Filter} from "../../../../../../definition/sensor/ScalarSensorConfig";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {MatSelectChange} from "@angular/material/select";
import {PlatformBusComponent} from "../../platform/platform-bus/platform-bus.component";
import {Lm75SensorConfig} from "../../../../../../definition/sensor/Lm75SensorConfig";
import {Lm75BusConfig} from "../../../../../../definition/platform/bus/Lm75BusConfig";
import {PlatformI2cComponent} from "../../platform/platform-i2c/platform-i2c.component";

@Component({
  selector: 'app-sensor-lm75',
  templateUrl: './sensor-lm75.component.html',
  styleUrls: ['./sensor-lm75.component.scss']
})
export class SensorLm75Component {
  nameCtrl = new FormControl('', Validators.required);
  busIdCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  updateIntervalCtrl = new FormControl('60s', Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    busId: this.busIdCtrl,
    showInHa: this.showInHaCtrl,
    updateInterval: this.updateIntervalCtrl
  });
  filters: Filter[] = [];

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorLm75Component>,
              @Inject(MAT_DIALOG_DATA) public data: { config: Lm75SensorConfig },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
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
    let config: Lm75SensorConfig = new Lm75SensorConfig(value.name as string, value.busId as string)
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: Lm75SensorConfig = this.data.config;
    config.name = value.name as string;
    config.busId = value.busId as string;
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close();
  }

  getBuses() {
    let buses = this.es.getBuses(PlatformType.LM75);
    return buses.sort()
  }


  onSelectSensor($event: MatSelectChange) {
    if ($event.value == "add") {
      $event.source.value = ""

      const dialogRefItem = this.dialog.open(PlatformI2cComponent, {data: {platformType: PlatformType.LM75}});

      dialogRefItem.afterClosed().subscribe(result => {
        if (result instanceof Lm75BusConfig) {
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

