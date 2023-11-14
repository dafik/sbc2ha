import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {LoggerConfig} from "../../../../../definition/LoggerConfig";
import {LogAddWriterComponent} from "../log-add-writer/log-add-writer.component";
import {LogAddDefaultComponent} from "../log-add-default/log-add-default.component";
import {LogLevel} from "../../../../../definition/enums/LogLevel";
import {LogAddPackageComponent} from "../log-add-package/log-add-package.component";

@Component({
    selector: 'app-log-list',
    templateUrl: './log-list.component.html',
    styleUrls: ['./log-list.component.scss']
})
export class LogListComponent {
    @Input() log!: LoggerConfig | undefined;
    @Output() newLog = new EventEmitter<boolean>();

    constructor(public dialog: MatDialog) {

    }

    deleteDefault() {
        delete this.log?.default
    }

    deleteWriter() {
        if (this.log) {
            this.log.writer = new Map();
            //delete this.log?.writer
        }
    }

    addLog() {
        this.newLog.next(true);
    }

    editPackage(key: string) {
        if (this.log) {
            const config: PackageLog = {
                level: (this.log.logs as any)[key],
                package: key
            }
            // @ts-ignore
            const dialogRefItem = this.dialog.open<LogAddPackageComponent, any, PackageLog>(LogAddPackageComponent, {data: config});
            dialogRefItem.afterClosed().subscribe(result => {
                if (result && this.log) {
                    // @ts-ignore
                    this.log.logs.set(result.package, result.level)
                }
            });
        }
    }

    deletePackage(key: string) {
        if (this.log) {
            // @ts-ignore
            this.log.logs.delete(key)
        }
    }

    getWriterFormat() {
        if (this.log?.writer) {
            let writer = this.log?.writer;
            // @ts-ignore
            if (writer?.get('format')) {
                // @ts-ignore
                return writer?.get('format');
            }
        }
        return null
    }

    addDefault() {
        const dialogRefItem = this.dialog.open<LogAddDefaultComponent, any, LogLevel>(LogAddDefaultComponent);
        dialogRefItem.afterClosed().subscribe(result => {
            if (result && this.log) {
                this.log.default = result
            }
        });
    }

    addPackage() {
        const dialogRefItem = this.dialog.open<LogAddPackageComponent, any, PackageLog>(LogAddPackageComponent);
        dialogRefItem.afterClosed().subscribe(result => {
            if (result && this.log) {
                // @ts-ignore
                this.log.logs.set(result.package,result.level);
            }
        });

    }

    addWriter() {
        const dialogRefItem = this.dialog.open<LogAddWriterComponent, any, string>(LogAddWriterComponent);
        dialogRefItem.afterClosed().subscribe(result => {
            if (result) {
                if (result && this.log) {

                    this.log.writer = new Map<string, string>([["format", result]]);
                }
            }
        });
    }

    editWriter() {
        if (this.log) {
            const dialogRefItem = this.dialog.open<LogAddWriterComponent, any, string>(LogAddWriterComponent, {data: this.log.writer?.get('format')});
            dialogRefItem.afterClosed().subscribe(result => {
                if (result && this.log) {
                    this.log.writer?.set('format', result);
                }
            });
        }
    }
}

export interface PackageLog {
    package: string,
    level: LogLevel
}
