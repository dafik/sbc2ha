import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {CoverConfig} from "../../../../definition/actuator/CoverConfig";
import {ExtensionsService} from "../../extensions.service";

@Component({
    selector: 'app-save-dialog',
    templateUrl: './save-dialog.component.html',
    styleUrls: ['./save-dialog.component.scss']
})
export class SaveDialogComponent {


    reloadCtrl = new FormControl(true, Validators.required);
    showLogsCtrl = new FormControl(true, Validators.required);


    platform = this._formBuilder.group({
        reload: this.reloadCtrl,
        showLogs: this.showLogsCtrl,
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<SaveDialogComponent, SaveCommand>,
                @Inject(MAT_DIALOG_DATA) public data: { config: CoverConfig },
                public es: ExtensionsService,
                public dialog: MatDialog
    ) {
    }


    uploadCache() {
        let rawValue = this.platform.getRawValue();
        this.dialogRef.close({
            target: "cache",
            reload: (rawValue.reload) as boolean,
            showLogs: (rawValue.showLogs) as boolean
        })
    }

    uploadConfig() {
        let rawValue = this.platform.getRawValue();
        this.dialogRef.close({
            target: "config",
            reload: (rawValue.reload) as boolean,
            showLogs: (rawValue.showLogs) as boolean
        })
    }
}

export interface SaveCommand {
    target: string;
    reload: boolean;
    showLogs: boolean
}
