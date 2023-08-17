import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {I2cBusConfig} from "../../../../../../definition/platform/bus/I2cBusConfig";
import {Ds2482BusConfig} from "../../../../../../definition/platform/bus/Ds2482BusConfig";
import {Mcp23017BusConfig} from "../../../../../../definition/platform/bus/Mcp23017BusConfig";
import {Lm75BusConfig} from "../../../../../../definition/platform/bus/Lm75BusConfig";
import {PCA9685BusConfig} from "../../../../../../definition/platform/bus/PCA9685BusConfig";

@Component({
  selector: 'app-platform-i2c',
  templateUrl: './platform-i2c.component.html',
  styleUrls: ['./platform-i2c.component.scss']
})
export class PlatformI2cComponent {
  busIdCtrl = new FormControl('', Validators.required);
  addressCtrl = new FormControl(0x20, Validators.required);
  platform = this._formBuilder.group({
    busId: this.busIdCtrl,
    uart: this.addressCtrl
  });

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<PlatformI2cComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: I2cBusConfig, platformType: PlatformType },) {
    if (data?.config) {
      this.busIdCtrl.patchValue(data.config.busId);
      this.addressCtrl.patchValue(data.config.address)
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;

    let config: I2cBusConfig;
    if (this.data.platformType == PlatformType.DS2482) {
      config = new Ds2482BusConfig(value.busId as string, value.uart as number)
    } else if (this.data.platformType == PlatformType.LM75) {
      config = new Lm75BusConfig(value.busId as string, value.uart as number)
    } else if (this.data.platformType == PlatformType.MCP23017) {
      config = new Mcp23017BusConfig(value.busId as string, value.uart as number)
    } else if (this.data.platformType == PlatformType.PCA9685) {
      config = new PCA9685BusConfig(value.busId as string, value.uart as number)
    } else {
      throw "bad bus config " + this.data.platformType
    }
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: I2cBusConfig = this.data.config;
    config.busId = value.busId as string


    this.dialogRef.close();
  }

}
