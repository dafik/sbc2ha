import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {ActuatorType} from "../../../../../../definition/enums/ActuatorType";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../../extensions.service";
import {Duration} from "../../../../../../definition/Duration";
import {CoverConfig} from "../../../../../../definition/actuator/CoverConfig";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {MatSelectChange} from "@angular/material/select";
import {BusConfig} from "../../../../../../definition/platform/bus/BusConfig";
import {PlatformI2cComponent} from "../../platform/platform-i2c/platform-i2c.component";

@Component({
  selector: 'app-actuator-cover',
  templateUrl: './actuator-cover.component.html',
  styleUrls: ['./actuator-cover.component.scss']
})
export class ActuatorCoverComponent {
  nameCtrl = new FormControl('', Validators.required);
  restoreStateCtrl = new FormControl(true, Validators.required);
  openRelayCtrl = new FormControl('', Validators.required);
  openRelayBusIdCtrl = new FormControl('', Validators.required);
  openRelayBusTypeCtrl = new FormControl(PlatformType.MCP23017, Validators.required);
  closeRelayCtrl = new FormControl('', Validators.required);
  closeRelayBusIdCtrl = new FormControl('', Validators.required);
  closeRelayBusTypeCtrl = new FormControl(PlatformType.MCP23017, Validators.required);
  openTimeCtrl = new FormControl('', Validators.required);
  closeTimeCtrl = new FormControl('', Validators.required);
  deviceClassCtrl = new FormControl('', Validators.required);
  showInHaCtrl = new FormControl(true, Validators.required);


  platform = this._formBuilder.group({
    name: this.nameCtrl,
    restoreState: this.restoreStateCtrl,
    openRelay: this.openRelayCtrl,
    openRelayBusId: this.openRelayBusIdCtrl,
    openRelayBusType: this.openRelayBusTypeCtrl,
    closeRelay: this.closeRelayCtrl,
    closeRelayBusId: this.closeRelayBusIdCtrl,
    closeRelayBusType: this.closeRelayBusTypeCtrl,
    openTime: this.openTimeCtrl,
    closeTime: this.closeTimeCtrl,
    deviceClass: this.deviceClassCtrl,
    showInHa: this.showInHaCtrl,
  });

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<ActuatorCoverComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: CoverConfig },
              public es: ExtensionsService,
              public dialog: MatDialog
  ) {
    if (data?.config) {
      this.nameCtrl.patchValue(data.config.name);
      this.restoreStateCtrl.patchValue(data.config.restoreState);
      this.openRelayCtrl.patchValue(data.config.openRelay);
      this.openRelayBusIdCtrl.patchValue(data.config.openRelayBusId);
      this.openRelayBusTypeCtrl.patchValue(PlatformType[data.config.openRelayBusType as keyof typeof PlatformType]);
      this.closeRelayCtrl.patchValue(data.config.closeRelay);
      this.closeRelayBusIdCtrl.patchValue(data.config.closeRelayBusId);
      this.closeRelayBusTypeCtrl.patchValue(PlatformType[data.config.closeRelayBusType as keyof typeof PlatformType]);
      this.openTimeCtrl.patchValue(data.config.openTime.duration);
      this.closeTimeCtrl.patchValue(data.config.closeTime.duration);
      this.deviceClassCtrl.patchValue(data.config.deviceClass);
      this.showInHaCtrl.patchValue(data.config.showInHa);
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: CoverConfig = new CoverConfig(
      value.name as string,
      ActuatorType.COVER,
      0,
      value.openRelay as string,
      value.openRelayBusId as string,
      value.openRelayBusType as string,
      value.closeRelay as string,
      value.closeRelayBusId as string,
      value.closeRelayBusType as string,
      new Duration(value.openTime as string),
      new Duration(value.closeTime as string),
      value.deviceClass as string
    )
    config.restoreState = value.restoreState as boolean

    config.showInHa = value.showInHa as boolean;
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: CoverConfig = this.data.config;
    config.name = value.name as string;
    config.outputType = ActuatorType.COVER;

    config.openRelay = value.openRelay as string;
    config.openRelayBusId = value.openRelayBusId as string;
    config.openRelayBusType = value.openRelayBusType as string;
    config.closeRelay = value.closeRelay as string;
    config.closeRelayBusId = value.closeRelayBusId as string;
    config.closeRelayBusType = value.closeRelayBusType as string;
    config.openTime = new Duration(value.openTime as string);
    config.closeTime = new Duration(value.closeTime as string);
    config.deviceClass = value.deviceClass as string;
    config.restoreState = value.restoreState as boolean
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

  getBusType() {
    return [PlatformType.MCP23017, PlatformType.PCA9685, PlatformType.GPIO]
  }

  getBus(fc: FormControl<string | null>) {
    let type = fc.value as PlatformType;
    return this.es.getBuses(type)
  }

  onSelectSensor($event: MatSelectChange, value: PlatformType | null) {
    if ($event.value == "add") {
      $event.source.value = ""

      if (value) {
        const dialogRefItem = this.dialog.open(PlatformI2cComponent, {data: {platformType: value}});

        dialogRefItem.afterClosed().subscribe(result => {
          if (result instanceof BusConfig) {
            this.es.addPlatform(result);
          }
          $event.source.open();
        });

      }
    }
  }

  protected readonly PlatformType = PlatformType;

    isValid() {
        return this.platform.valid
    }
}
