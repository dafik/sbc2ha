import {Component} from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";

@Component({
    selector: 'app-web-only',
    templateUrl: './web-only.component.html',
    styleUrls: ['./web-only.component.scss']
})
export class WebOnlyComponent {


    constructor(public dialogRef: MatDialogRef<WebOnlyComponent>,) {
    }

    ok() {
        this.dialogRef.close();
    }
}
