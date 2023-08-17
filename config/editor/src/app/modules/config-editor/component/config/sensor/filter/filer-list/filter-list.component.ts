import {Component, Input} from '@angular/core';
import {Filter} from "../../../../../../../definition/sensor/ScalarSensorConfig";
import {MatDialog} from "@angular/material/dialog";
import {FilterAddComponent} from "../filter-add/filter-add.component";

@Component({
  selector: 'app-filter-list',
  templateUrl: './filter-list.component.html',
  styleUrls: ['./filter-list.component.scss']
})
export class FilterListComponent {
  @Input({required: true}) filters!: Filter[];

  constructor(public dialog: MatDialog) {
  }

  addFilter() {
    const dialogRef = this.dialog.open(FilterAddComponent);

    dialogRef.afterClosed().subscribe(result => {
     this.filters.push(result)
    });
  }

  edit(filter: Filter) {
    const dialogRef = this.dialog.open(FilterAddComponent, {
      data: {filter},
    });
  }

  delete(i: number) {
    this.filters.splice(i, 1);
  }
}
