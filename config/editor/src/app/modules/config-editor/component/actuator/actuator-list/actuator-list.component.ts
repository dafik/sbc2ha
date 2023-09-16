import {Component, Input, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {ActuatorConfig} from "../../../../../definition/actuator/ActuatorConfig";
import {ActuatorAddComponent} from "../actuator-add/actuator-add.component";
import {selectableActuatorDefinition} from "../actuator-add";
import {FormBuilder, FormControl} from "@angular/forms";
import {SensorConfig} from "../../../../../definition/sensor/SensorConfig";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'app-actuator-list',
  templateUrl: './actuator-list.component.html',
  styleUrls: ['./actuator-list.component.scss']
})
export class ActuatorListComponent implements OnInit{
  @Input({required: true})
  actuators!: ActuatorConfig[];
  private actuators$$ = new BehaviorSubject(this.actuators)
  actuators$= this.actuators$$.asObservable()
  nameCtrl = new FormControl('');
  filter = this._formBuilder.group({
    name: this.nameCtrl
  });


  constructor(private _formBuilder: FormBuilder,private dialog: MatDialog) {
    this.nameCtrl.valueChanges.subscribe(filter => {
      if (filter) {
        let sensorConfigs = this.actuators.filter(sensor => {
          return sensor.name.toLowerCase().includes(filter.toLowerCase());
        });
        this.actuators$$.next(sensorConfigs);
      }else {
        this.actuators$$.next(this.actuators)
      }

    })
  }

  ngOnInit(): void {
    this.actuators$$.next(this.actuators);
  }

  addActuator() {
    const dialogRef = this.dialog.open(ActuatorAddComponent);
  }

  edit(config: ActuatorConfig) {
    let def = selectableActuatorDefinition.find(value => {
      return true;
      //return value.type == config.outputType
    });
    if (def){
      const dialogRef = this.dialog.open(def.component, {
        data: {config: config},
      });
    }
  }

  delete(i: number) {
    this.actuators.splice(i, 1);
  }
}
