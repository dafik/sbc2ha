import {Component, Input, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {SensorAddComponent} from "../sensor-add/sensor-add.component";
import {SensorConfig} from "../../../../../definition/sensor/SensorConfig";
import {selectableSensorDefinition} from "../sensor-add";
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {BehaviorSubject, map, Observable} from "rxjs";

@Component({
    selector: 'app-sensor-list',
    templateUrl: './sensor-list.component.html',
    styleUrls: ['./sensor-list.component.scss']
})
export class SensorListComponent implements OnInit{
    @Input({required: true})
    sensors!: SensorConfig[];

    private sensors$$ = new BehaviorSubject(this.sensors)
    sensors$= this.sensors$$.asObservable()


    nameCtrl = new FormControl('');
    filter = this._formBuilder.group({
        name: this.nameCtrl
    });

    constructor(private _formBuilder: FormBuilder, private dialog: MatDialog) {

        this.nameCtrl.valueChanges.subscribe(filter => {
                if (filter) {
                    let sensorConfigs = this.sensors.filter(sensor => {
                        return sensor.name.toLowerCase().includes(filter.toLowerCase());
                    });
                    this.sensors$$.next(sensorConfigs);
                }else {
                    this.sensors$$.next(this.sensors)
                }

            })
    }

    ngOnInit(): void {
        this.sensors$$.next(this.sensors);
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

    onFilter($event: Event) {
        console.log($event)
    }
}
