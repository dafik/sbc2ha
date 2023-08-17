import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-actions-add-type',
  templateUrl: './actions-add-type.component.html',
  styleUrls: ['./actions-add-type.component.scss']
})
export class ActionsAddTypeComponent {

  constructor(
    public dialogRef: MatDialogRef<ActionsAddTypeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { types: string[] },
    public dialog: MatDialog
  ) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  getSelectableList() {
    let selectableItems = this.data.types;

    return selectableItems;
  }

  addType(item: string) {
    this.dialogRef.close(item)
  }
}


