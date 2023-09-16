import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ExtensionsService} from "../../modules/config-editor/extensions.service";
import {SensorConfig} from "../../definition/sensor/SensorConfig";
import {ActuatorConfig} from "../../definition/actuator/ActuatorConfig";
import {Subject, takeUntil} from "rxjs";
import {environment} from "../../../environments/environment";
import {MatSnackBar} from "@angular/material/snack-bar";
import {StateService} from "./state.service";
import {MatSlideToggleChange} from "@angular/material/slide-toggle";
import {InputSensorConfig} from "../../definition/sensor/digital/InputSensorConfig";
import {InputSwitchConfig} from "../../definition/sensor/digital/InputSwitchConfig";
import {ButtonState} from "../../definition/enums/ButtonState";
import {MatSelectChange} from "@angular/material/select";
import {InputSensorAction} from "../../definition/enums/InputSensorAction";

@Component({
    selector: 'app-states',
    templateUrl: './states.component.html',
    styleUrls: ['./states.component.scss']
})
export class StatesComponent implements OnInit, OnDestroy {
    private sensors = new Subject<SensorConfig[]>();
    private actuators = new Subject<ActuatorConfig[]>();

    public sensors$ = this.sensors.asObservable();
    public actuators$ = this.actuators.asObservable();

    destroyed$ = new Subject<boolean>();
    private states: InitialState[] = [];

    private fakeEvent: Map<string, string> = new Map<string, string>();

    constructor(private es: ExtensionsService,
                private snackBar: MatSnackBar,
                private stateService: StateService,
                private cdk: ChangeDetectorRef
    ) {
    }

    ngOnInit(): void {
        this.es.getCurrentConfig()
            .pipe(
                takeUntil(this.destroyed$)
            )
            .subscribe(value => {
                this.sensors.next(value.sensor);
                this.actuators.next(value.actuator);
            })
        this.sensors$
            .pipe(
                takeUntil(this.destroyed$)
            )
            .subscribe(values => {
            values.forEach(config => {
                this.fakeEvent.set(config.name, "");
            })
        })

        if (environment.webOnly!) {
            this.snackBar.open("States unavailable in webOnly preview", "Ok, i understand", {panelClass: 'error'})
        } else {
            this.stateService.connect()
                .pipe(
                    takeUntil(this.destroyed$)
                )
                .subscribe(message => {
                    if (Array.isArray(message)) {
                        this.onInitial(message);
                    } else {
                        this.onState(message);
                    }
                    console.log(message)
                    this.cdk.detectChanges();
                });
        }
    }

    private onState(state: State) {
        if (state.type == "actuator") {
            let find = this.states.find(value => {
                return value.type == state.type && value.name == state.id
            });
            if (find) {
                find.state = state.state.state == "ON" ? "1" : "0";
            }
        } else if (state.type == "sensor") {
            let find = this.states.find(value => {
                return value.type == state.type && value.name == state.name
            });
            if (!find) {
                find = {type: "sensor", name: state.name, state: state.state.state};
                this.states.push(find)
            }
            if (state.state.value != null) {
                find.state = state.state.value
            }

        }

    }

    private onInitial(message: InitialState[]) {
        this.states = message;
    }

    ngOnDestroy(): void {
        this.destroyed$.next(true);
    }

    onChange($event: MatSlideToggleChange, output: number) {
        console.log('toogle: ' + output + " state:" + $event.checked)
        const message = {
            type: "gpio",
            actuator: output.toString(),
            state: $event.checked ? "on" : "off"
        }
        this.stateService.send(message);
    }

    getActuatorState(name: string) {
        let find = this.states.find(value => value.name == name && value.type == "actuator");
        if (find) {
            return parseFloat(find.state) > 0;
        }
        return false
    }

    getSensorState(name: string) {
        let find = this.states.find(value => value.name == name && value.type == "sensor");
        if (find) {
            return find.state;
        }
        return ""
    }

    isSwitch(sensor: SensorConfig) {
        return sensor instanceof InputSwitchConfig
    }

    isDigital(sensor: SensorConfig) {
        return sensor instanceof InputSensorConfig;
    }

    isBinary(sensor: SensorConfig) {
        let b1 = this.isSwitch(sensor);
        let b2 = this.isDigital(sensor);
        let b = b1 || b2;
        return b
    }

    protected readonly ButtonState = ButtonState;

    onStateChange($event: MatSelectChange, sensor: any) {
        let value = $event.value;
        this.fakeEvent.set(sensor.name, value);
    }

    sendEvent(sensor: SensorConfig) {

        let ev = this.fakeEvent.get(sensor.name);
        if (sensor instanceof InputSwitchConfig) {
            let x = {
                sensor: sensor.name,
                state: ev,
                type: "button"
            }
            this.stateService.send(x);
        } else if (sensor instanceof InputSensorConfig) {
            let x = {
                sensor: sensor.name,
                state: ev,
                type: "binary"
            }
            this.stateService.send(x);
        }


    }

    isStateSelected(sensor: SensorConfig) {
        let ev = this.fakeEvent.get(sensor.name);
        if (ev !== null) {
            let b = ev != "";
            return b;
        }
        return false
    }

    protected readonly InputSensorAction = InputSensorAction;
}

interface InitialState {
    type: 'actuator' | 'sensor';
    name: string;
    state: string;
}

interface State {
    type: 'actuator' | 'sensor';
    name: string;
    id: string
    state: {
        type: string;
        state: string;
        value?: string;
    }
}
