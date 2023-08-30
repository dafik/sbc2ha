import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {LogPlaceholder, logPlaceholderData} from "./logPlaceholder";

@Component({
    selector: 'app-log-add-writer',
    templateUrl: './log-add-writer.component.html',
    styleUrls: ['./log-add-writer.component.scss']
})
export class LogAddWriterComponent {
    formatCtrl = new FormControl('', Validators.required);
    placeHolderCtrl = new FormControl(LogPlaceholder.MESSAGE, Validators.required);
    platform = this._formBuilder.group({
        format: this.formatCtrl,
        placeholder: this.placeHolderCtrl,
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<LogAddWriterComponent>,
                @Inject(MAT_DIALOG_DATA) public data: string,
    ) {
        if (data) {
            this.formatCtrl.patchValue(data);
        }
    }

    onNoClick() {
        this.dialogRef.close();
    }

    add() {
        let value = this.platform.value;
        this.dialogRef.close(value.format);
    }

    protected readonly LogPlaceholder = LogPlaceholder;

    getPlaceholders() {
        let strings = Object.values(LogPlaceholder);
        return strings
    }

    getPlaceholder() {
        if (this.placeHolderCtrl.value) {
            let placeholder: LogPlaceholder = this.placeHolderCtrl.value;
            return logPlaceholderData[placeholder]
        }
        return null
    }

    addPlaceholder() {
        let placeholder = this.getPlaceholder();
        if (placeholder) {
            let value = this.formatCtrl.value;
            let newVar = value ? value + " " + placeholder.value : placeholder.value;
            this.formatCtrl.patchValue(newVar)
        }
    }

    isValid() {
        return this.platform.valid
    }
}
