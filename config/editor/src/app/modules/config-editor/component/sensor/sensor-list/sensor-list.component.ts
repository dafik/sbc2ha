import {Component, Input} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {SensorAddComponent} from "../sensor-add/sensor-add.component";
import {SensorConfig} from "../../../../../definition/sensor/SensorConfig";
import {selectableSensorDefinition} from "../sensor-add";

@Component({
    selector: 'app-sensor-list',
    templateUrl: './sensor-list.component.html',
    styleUrls: ['./sensor-list.component.scss']
})
export class SensorListComponent {
    @Input({required: true}) sensors!: SensorConfig[];


    constructor(public dialog: MatDialog) {
    }

    addSensor() {
        this.dialog.open(SensorAddComponent);
    }

    edit(config: SensorConfig) {
        let def = selectableSensorDefinition.find(value => {
            return value.type == config.platform
        });
        if (def) {
            const dialogRef = this.dialog.open(def.component, {
                data: {config: config},
            });
        }
    }

    delete(i: number) {
        this.sensors.splice(i, 1);
    }
}
