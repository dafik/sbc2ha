import {Component, Input} from '@angular/core';
import {PlatformConfig} from "../../../../../definition/platform/PlatformConfig";
import {MatDialog} from "@angular/material/dialog";
import {PlatformAddComponent} from "../platform-add/platform-add.component";
import {selectableDefinition} from "../platform-add";
import {BusConfig} from "../../../../../definition/platform/bus/BusConfig";
import {I2cBusConfig} from "../../../../../definition/platform/bus/I2cBusConfig";

@Component({
    selector: 'app-platform-list',
    templateUrl: './platform-list.component.html',
    styleUrls: ['./platform-list.component.scss']
})
export class PlatformListComponent {
    @Input({required: true}) platforms!: PlatformConfig[];

    constructor(public dialog: MatDialog) {
    }

    addPlatform() {
        const dialogRef = this.dialog.open(PlatformAddComponent);
    }

    edit(config: PlatformConfig) {
        let def = selectableDefinition.find(value => {
            return value.type == config.platform
        });
        if (def) {
            const dialogRef = this.dialog.open(def.component, {
                data: {config: config, platformType: config.platform},
            });
        }
    }

    delete(i: number) {
        this.platforms.splice(i, 1);
    }

    protected readonly BusConfig = BusConfig;

    isBusConfig(platform: PlatformConfig) {
        return platform instanceof BusConfig;
    }

    asBusConfig(platform: PlatformConfig) {
        if(platform instanceof BusConfig){
            return platform as BusConfig
        }
        return undefined
    }

    asI2cBusConfig(platform: PlatformConfig) {
        if(platform instanceof I2cBusConfig){
            return platform as I2cBusConfig
        }
        return undefined
    }

    isI2cBusConfig(platform: PlatformConfig) {
        return platform instanceof I2cBusConfig;
    }
}
