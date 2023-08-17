import {Component} from '@angular/core';
import {LogLevel} from "../../../../../definition/enums/LogLevel";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {zero} from "../../../../../shared/zero";

@Component({
    selector: 'app-log-add-default',
    templateUrl: './log-add-default.component.html',
    styleUrls: ['./log-add-default.component.scss']
})
export class LogAddDefaultComponent {

    protected readonly LogLevel = LogLevel;


    constructor(
        private dialogRef: MatDialogRef<LogAddDefaultComponent>,
        private dialog: MatDialog,
    ) {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }


    selectLevel(value: LogLevel) {
        this.dialogRef.close(value);
    }

    protected readonly zero = zero;
}
