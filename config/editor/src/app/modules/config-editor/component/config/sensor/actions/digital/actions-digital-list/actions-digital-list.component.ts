import {Component, Input} from '@angular/core';
import {Filter} from "../../../../../../../../definition/sensor/ScalarSensorConfig";
import {MatDialog} from "@angular/material/dialog";
import {FilterAddComponent} from "../../../filter/filter-add/filter-add.component";

@Component({
  selector: 'app-actions-digital-list',
  templateUrl: './actions-digital-list.component.html',
  styleUrls: ['./actions-digital-list.component.scss']
})
export class ActionsDigitalListComponent {
  @Input({required: true}) actions!: Filter[];

  constructor(public dialog: MatDialog) {
  }

  addFilter() {
    const dialogRef = this.dialog.open(FilterAddComponent);

    dialogRef.afterClosed().subscribe(result => {
      this.actions.push(result)
    });
  }

  edit(filter: Filter) {
    const dialogRef = this.dialog.open(FilterAddComponent, {
      data: {filter},
    });
  }

  delete(i: number) {
    this.actions.splice(i, 1);
  }
}


