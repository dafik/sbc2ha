import {Component} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {definition} from "../../../../definition/extentsionBoard/ExtensionBoardsDef";

@Component({
  selector: 'app-creator',
  templateUrl: './creator.component.html',
  styleUrls: ['./creator.component.scss']
})
export class CreatorComponent {
  constructor(private _formBuilder: FormBuilder) {
  }

  vendorFormGroup: FormGroup = this._formBuilder.group({vendorCtrl: ['']});
  extensionsFormGroup: FormGroup = this._formBuilder.group({inputCtrl: [''], outputCtrl: ['']});

  public getVendors() {
    return definition.vendor.map(item => item.name)
  }

  public getInputs() {
    const vendor = this.vendorFormGroup.get("vendorCtrl")?.value
    return definition.vendor
      .filter(v => v.name == vendor)
      .map(v => v.inputBoard
        .map(ib => ib.name))
  }

  public getOutputs() {
    const vendor = this.vendorFormGroup.get("vendorCtrl")?.value
    return definition.vendor
      .filter(v => v.name == vendor)
      .map(v => v.outputBoard
        .map(ob => ob.name))
  }
}
