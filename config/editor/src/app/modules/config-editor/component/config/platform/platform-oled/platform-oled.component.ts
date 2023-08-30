import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MqttConfig} from "../../../../../../definition/platform/MqttConfig";
import {OledConfig} from "../../../../../../definition/platform/OledConfig";
import {ScreenType} from "../../../../../../definition/enums/ScreenType";
import {Duration} from "../../../../../../definition/Duration";

@Component({
  selector: 'app-platform-oled',
  templateUrl: './platform-oled.component.html',
  styleUrls: ['./platform-oled.component.scss']
})
export class PlatformOledComponent {
  enabledCtrl = new FormControl(true, Validators.required);
  screensCtrl = new FormControl<ScreenType[]>([], Validators.required);
  screensaverTimeoutCtrl = new FormControl('', Validators.required);

  platform = this._formBuilder.group({
    enabled: this.enabledCtrl,
    screens: this.screensCtrl,
    screensaverTimeout: this.screensaverTimeoutCtrl,
  });

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<PlatformOledComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { config: OledConfig },) {
    if (data?.config) {
      this.enabledCtrl.patchValue(data.config.enabled);
      this.screensCtrl.patchValue(data.config.screens);
      this.screensaverTimeoutCtrl.patchValue(data.config.screensaverTimeout.duration);
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: OledConfig = new OledConfig()
    config.enabled = value.enabled as boolean;
    config.screens = value.screens as ScreenType[];
    config.screensaverTimeout = new Duration(value.screensaverTimeout as string);

    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: OledConfig = this.data.config;
    config.enabled = value.enabled as boolean;
    config.screens = value.screens as ScreenType[];
    config.screensaverTimeout = new Duration(value.screensaverTimeout as string);

    this.dialogRef.close();
  }

  protected readonly ScreenType = ScreenType;

    isValid() {
      return this.platform.valid
    }
}
