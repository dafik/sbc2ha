import {Component} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {definition} from "../../../../definition/extentsionBoard/ExtensionBoardsDef";
import {filter, map} from "rxjs";
import {Router} from "@angular/router";


@Component({
  selector: 'app-creator',
  templateUrl: './creator.component.html',
  styleUrls: ['./creator.component.scss']
})
export class CreatorComponent {
  vendors = definition.vendor;

  vendorCtrl = new FormControl('', Validators.required);
  inputCtrl = new FormControl('', Validators.required);
  outputCtrl = new FormControl('', Validators.required);

  vendorFG = this._formBuilder.group({
    vendorCtrl: this.vendorCtrl,
  });
  extensionsFG = this._formBuilder.group({
    inputCtrl: this.inputCtrl,
    outputCtrl: this.outputCtrl,
  });
  inputs: string[] = [];
  outputs: string[] = [];

  constructor(private _formBuilder: FormBuilder, private router: Router) {
    this.vendorCtrl.valueChanges.pipe(
      filter(Boolean),
      map(v => definition.vendor.find(value => value.name == v)),
      filter(Boolean)
    ).subscribe(vendor => {
      this.inputs = vendor.inputBoard.map(ib => ib.name);
      this.outputs = vendor.outputBoard.map(ib => ib.name);
    })
  }

  public getVendors() {
    return definition.vendor.map(item => item.name)
  }


  edit() {
    this.router.navigate(["/editor"], {
      queryParams: {
        v: this.vendorCtrl.value,
        i: this.inputCtrl.value,
        o: this.outputCtrl.value
      }
    });
  }
}
