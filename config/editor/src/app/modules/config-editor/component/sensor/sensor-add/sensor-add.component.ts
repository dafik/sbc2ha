import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ComponentType} from "@angular/cdk/portal";
import {selectableSensorDefinition, SelectableSensorItem} from "../sensor-add";
import {SensorConfig} from "../../../../../definition/sensor/SensorConfig";
import {ExtensionsService} from "../../../extensions.service";


@Component({
    selector: 'app-sensor-add',
    templateUrl: './sensor-add.component.html',
    styleUrls: ['./sensor-add.component.scss']
})
export class SensorAddComponent {

    constructor(
        private dialogRef: MatDialogRef<SensorAddComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { sensors: SensorConfig[] },
        private dialog: MatDialog,
        private es: ExtensionsService
    ) {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }

    getSelectableList() {
        let selectableItems = selectableSensorDefinition.filter(item => {
            return true;
            //return !this.isSelected(item.type)
        })

        return selectableItems;
    }


    openSensor(item: SelectableSensorItem) {
        const dialogRefItem = this.dialog.open<any, any, SensorConfig>(item.component, {data: {platformType: item.type}});

        dialogRefItem.afterOpened().subscribe(value => {
            this.dialogRef.close();
        })

        dialogRefItem.afterClosed().subscribe(result => {
            if (result instanceof SensorConfig) {
                this.es.addSensor(result);
            }
        });
    }
}


