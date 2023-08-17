import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Filter} from "../../../../../../definition/sensor/ScalarSensorConfig";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {MatSelectChange} from "@angular/material/select";
import {Lm75BusConfig} from "../../../../../../definition/platform/bus/Lm75BusConfig";
import {ModbusSensorConfig} from "../../../../../../definition/sensor/ModbusSensorConfig";
import {PlatformModbusComponent} from "../../platform/platform-modbus/platform-modbus.component";
import {ModbusBusConfig} from "../../../../../../definition/platform/bus/ModbusBusConfig";

@Component({
  selector: 'app-sensor-modbus',
  templateUrl: './sensor-modbus.component.html',
  styleUrls: ['./sensor-modbus.component.scss']
})
export class SensorModbusComponent {
  nameCtrl = new FormControl('', Validators.required);
  busIdCtrl = new FormControl('', Validators.required);
  addressCtrl = new FormControl('', Validators.required);
  modelCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);
  updateIntervalCtrl = new FormControl('60s', Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    busId: this.busIdCtrl,
    address: this.addressCtrl,
    model: this.modelCtrl,
    showInHa: this.showInHaCtrl,
    updateInterval: this.updateIntervalCtrl
  });
  filters: Filter[] = [];

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<SensorModbusComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: ModbusSensorConfig },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.busIdCtrl.patchValue(data.config.busId);
      this.addressCtrl.patchValue(data.config.address.toString())
      this.modelCtrl.patchValue(data.config.model)
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
    let config: ModbusSensorConfig = new ModbusSensorConfig(value.name as string, value.busId as string, Number(value.address), value.model as string)
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string
    config.showInHa = value.showInHa as boolean;
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: ModbusSensorConfig = this.data.config;
    config.name = value.name as string;
    config.busId = value.busId as string;
    config.address = Number(value.address);
    config.model = value.model as string;
    config.filters = this.filters;
    config.updateInterval.duration = value.updateInterval as string;
    config.showInHa = value.showInHa as boolean;

    this.dialogRef.close();
  }

  getBuses() {
    let buses = this.es.getBuses(PlatformType.MODBUS);
    return buses.sort()
  }


  onSelectSensor($event: MatSelectChange) {
    if ($event.value == "add") {
      $event.source.value = ""

      const dialogRefItem = this.dialog.open(PlatformModbusComponent, {data: {platformType: PlatformType.MODBUS}});

      dialogRefItem.afterClosed().subscribe(result => {
        if (result instanceof ModbusBusConfig) {
          this.es.addPlatform(result);
        }
        $event.source.open();
      });


    }
  }

  getModels() {
    return ["sdm630"]
  }
}


