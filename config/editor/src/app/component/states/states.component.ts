import {Component, OnInit} from '@angular/core';
import {ExtensionsService} from "../../modules/config-editor/extensions.service";
import {SensorConfig} from "../../definition/sensor/SensorConfig";
import {ActuatorConfig} from "../../definition/actuator/ActuatorConfig";
import {Subject} from "rxjs";

@Component({
    selector: 'app-states',
    templateUrl: './states.component.html',
    styleUrls: ['./states.component.scss']
})
export class StatesComponent implements OnInit {
    private sensors = new Subject<SensorConfig[]>();
    private actuators = new Subject<ActuatorConfig[]>();

    public sensors$ = this.sensors.asObservable();
    public actuators$ = this.actuators.asObservable();

    constructor(private es: ExtensionsService) {
    }

    ngOnInit(): void {
        this.es.getCurrentConfig().subscribe(value => {
            this.sensors.next(value.sensor);
            this.actuators.next(value.actuator);
        })
    }


}
