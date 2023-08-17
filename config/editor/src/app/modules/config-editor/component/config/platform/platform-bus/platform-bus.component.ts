import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {BusConfig} from "../../../../../../definition/platform/bus/BusConfig";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {DallasBusConfig} from "../../../../../../definition/platform/bus/DallasBusConfig";

@Component({
  selector: 'app-platform-bus',
  templateUrl: './platform-bus.component.html',
  styleUrls: ['./platform-bus.component.scss']
})
export class PlatformBusComponent {
  busIdCtrl = new FormControl('', Validators.required);
  platform = this._formBuilder.group({
    busId: this.busIdCtrl,
  });

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<PlatformBusComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: BusConfig, platformType: PlatformType },) {
    if (data?.config) {
      this.busIdCtrl.patchValue(data.config.busId);
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;

    let config: BusConfig;
    if (this.data.platformType == PlatformType.DALLAS) {
      config = new DallasBusConfig(value.busId as string)
    } else {
      throw "bad bus config " + this.data.platformType
    }
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: BusConfig = this.data.config;
    config.busId = value.busId as string


    this.dialogRef.close();
  }

}
