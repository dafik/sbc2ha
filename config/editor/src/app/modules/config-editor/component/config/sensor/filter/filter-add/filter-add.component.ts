import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Filter} from "../../../../../../../definition/sensor/ScalarSensorConfig";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {AnalogSensorConfig} from "../../../../../../../definition/sensor/analog/AnalogSensorConfig";
import {ValueFilterType} from "../../../../../../../definition/enums/ValueFilterType";
import {UartType} from "../../../../../../../definition/enums/UartType";

@Component({
  selector: 'app-filter-add',
  templateUrl: './filter-add.component.html',
  styleUrls: ['./filter-add.component.scss']
})
export class FilterAddComponent {
  typeCtrl = new FormControl(ValueFilterType.ROUND, Validators.required);
  valueCtrl = new FormControl(1, Validators.required);



  platform = this._formBuilder.group({
    type: this.typeCtrl,
    value: this.valueCtrl,

  });
  filters: Filter[] = [];

  constructor(private _formBuilder: FormBuilder,
              public dialogRef: MatDialogRef<FilterAddComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { filter: Filter },) {
    if (data?.filter) {
      this.typeCtrl.patchValue(data.filter.type);
      this.valueCtrl.patchValue(data.filter.value);
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  add() {
    let value = this.platform.value;
    let config: Filter = new Filter(value.type as ValueFilterType, value.value as number)
    this.dialogRef.close(config);
  }

  edit() {
    let value = this.platform.value;
    let config: Filter = this.data.filter;
    config.type = value.type as ValueFilterType;
    config.value = value.value as number;

    this.dialogRef.close();
  }

  protected readonly UartType = UartType;
  protected readonly ValueFilterType = ValueFilterType;

    isValid() {
      return this.platform.valid
    }
}
