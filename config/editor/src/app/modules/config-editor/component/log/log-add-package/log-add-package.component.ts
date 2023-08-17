import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ExtensionsService} from "../../../extensions.service";
import {LogLevel} from "../../../../../definition/enums/LogLevel";
import {PackageLog} from "../log-list/log-list.component";

@Component({
    selector: 'app-log-add-package',
    templateUrl: './log-add-package.component.html',
    styleUrls: ['./log-add-package.component.scss']
})
export class LogAddPackageComponent {
    packageCtrl = new FormControl('', Validators.required);
    levelCtrl = new FormControl(LogLevel.WARN, Validators.required);
    platform = this._formBuilder.group({
        package: this.packageCtrl,
        level: this.levelCtrl,
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<LogAddPackageComponent>,
                @Inject(MAT_DIALOG_DATA) public data: PackageLog,
    ) {
        if (data) {
            this.packageCtrl.patchValue(data.package);
            this.levelCtrl.patchValue(data.level);
        }
    }

    onNoClick() {
        this.dialogRef.close();
    }

    add() {
        let value = this.platform.value;
        let config: PackageLog = {
            package: value.package as string,
            level: value.level as LogLevel
        }
        this.dialogRef.close(config);
    }

    protected readonly LogLevel = LogLevel;
}
