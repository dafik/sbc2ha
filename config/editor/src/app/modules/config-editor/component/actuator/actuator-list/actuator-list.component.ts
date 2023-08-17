import {Component, Input} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {ActuatorConfig} from "../../../../../definition/actuator/ActuatorConfig";
import {ActuatorAddComponent} from "../actuator-add/actuator-add.component";
import {selectableActuatorDefinition} from "../actuator-add";

@Component({
  selector: 'app-actuator-list',
  templateUrl: './actuator-list.component.html',
  styleUrls: ['./actuator-list.component.scss']
})
export class ActuatorListComponent {
  @Input({required: true}) actuators!: ActuatorConfig[];


  constructor(public dialog: MatDialog) {
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
