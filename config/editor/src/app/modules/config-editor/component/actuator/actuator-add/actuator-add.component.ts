import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ComponentType} from "@angular/cdk/portal";
import {ActuatorConfig} from "../../../../../definition/actuator/ActuatorConfig";
import {selectableActuatorDefinition, SelectableActuatorItem} from "../actuator-add";
import {ExtensionsService} from "../../../extensions.service";

@Component({
    selector: 'app-actuator-add',
    templateUrl: './actuator-add.component.html',
    styleUrls: ['./actuator-add.component.scss']
})
export class ActuatorAddComponent {

    constructor(
        private dialogRef: MatDialogRef<ActuatorAddComponent>,
        private dialog: MatDialog,
        private es: ExtensionsService
    ) {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }

    getSelectableList() {
        return selectableActuatorDefinition
    }

    openActuator(item: SelectableActuatorItem) {
        const dialogRefItem = this.dialog
            .open<any, any, ActuatorConfig>(item.component, {
                data: {
                    kind: item.kind
                }
            });
        dialogRefItem.afterOpened().subscribe(value => {
            this.dialogRef.close()
        })

        dialogRefItem.afterClosed()
            .subscribe(config => {
                if (config instanceof ActuatorConfig) {
                    this.es.addActuator(config)
                }
            });
    }
}


