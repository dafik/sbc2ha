import {Component, OnInit} from '@angular/core';
import {environment} from "../../../../environments/environment";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatDialog} from "@angular/material/dialog";
import {WebOnlyComponent} from "../web-only/web-only.component";

@Component({
    selector: 'app-layout',
    templateUrl: './layout.component.html',
    styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {


    constructor(private _snackBar: MatSnackBar,
                private dialog: MatDialog
    ) {
    }

    ngOnInit(): void {
        if (environment.webOnly!) {
            console.log("webonly")
            this.dialog.open(WebOnlyComponent)
        }
    }
}
