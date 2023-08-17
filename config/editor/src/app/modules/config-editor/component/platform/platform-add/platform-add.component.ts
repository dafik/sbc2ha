import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {PlatformConfig} from "../../../../../definition/platform/PlatformConfig";
import {PlatformType} from "../../../../../definition/enums/PlatformType";
import {ComponentType} from "@angular/cdk/portal";
import {selectableDefinition, SelectablePlatformItem} from "../platform-add";
import {ExtensionsService} from "../../../extensions.service";


@Component({
    selector: 'app-platform-add',
    templateUrl: './platform-add.component.html',
    styleUrls: ['./platform-add.component.scss']
})
export class PlatformAddComponent {

    constructor(
        private dialogRef: MatDialogRef<PlatformAddComponent>,
        private dialog: MatDialog,
        private es: ExtensionsService
    ) {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }

    getSelectableList() {
        return selectableDefinition.filter(item => {
            if (!item.single) {
                return true;
            }
            return !this.isSelected(item.type)
        });
    }

    isSelected(name: PlatformType): boolean {
        for (const value of this.es.getPlatforms()) {
            if (value.platform == name) {
                return true
            }
        }
        return false;
    }

    openPlatform(item: SelectablePlatformItem) {
        const dialogRefItem = this.dialog.open<any, any, PlatformConfig>(item.component, {data: {platformType: item.type}});

        dialogRefItem.afterOpened().subscribe(value => {
            this.dialogRef.close();
        })

        dialogRefItem.afterClosed().subscribe(result => {
            if (result instanceof PlatformConfig) {
                this.es.addPlatform(result)
            }
        });
    }
}


