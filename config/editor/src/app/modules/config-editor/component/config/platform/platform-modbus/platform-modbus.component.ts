import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {PlatformType} from "../../../../../../definition/enums/PlatformType";
import {ModbusBusConfig} from "../../../../../../definition/platform/bus/ModbusBusConfig";
import {UartType} from "../../../../../../definition/enums/UartType";
import {zero} from "../../../../../../shared/zero";

@Component({
    selector: 'app-platform-modbus',
    templateUrl: './platform-modbus.component.html',
    styleUrls: ['./platform-modbus.component.scss']
})
export class PlatformModbusComponent {
    busIdCtrl = new FormControl('', Validators.required);
    uartCtrl = new FormControl<UartType>(UartType.UART4, Validators.required);
    platform = this._formBuilder.group({
        busId: this.busIdCtrl,
        uart: this.uartCtrl
    });

    constructor(private _formBuilder: FormBuilder,
                public dialogRef: MatDialogRef<PlatformModbusComponent>,
                @Inject(MAT_DIALOG_DATA) public data: { config: ModbusBusConfig, platformType: PlatformType },) {
        if (data?.config) {
            this.busIdCtrl.patchValue(data.config.busId);
            this.uartCtrl.patchValue(data.config.uart)
        }
    }

    onNoClick() {
        this.dialogRef.close();
    }

    add() {
        let value = this.platform.value;
        let config = new ModbusBusConfig(value.uart as UartType)
        config.busId = value.busId as string;
        this.dialogRef.close(config);
    }

    edit() {
        let value = this.platform.value;
        let config: ModbusBusConfig = this.data.config;
        config.uart = value.uart as UartType;
        config.busId = value.busId as string


        this.dialogRef.close();
    }

    protected readonly UartType = UartType;
    protected readonly zero = zero;
}
