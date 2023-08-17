import {Component, OnDestroy, OnInit} from '@angular/core';
import {RemoteLogService} from "./remote-log.service";
import {Observable, Subject, takeUntil} from "rxjs";
import {LogMessage} from "../../modules/log-monitor/models/log-message.model";
import {DatePipe} from "@angular/common";
import {LenghtPipe} from "../../shared/lenght.pipe";

@Component({

    selector: 'app-logs',
    templateUrl: './logs.component.html',
    styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnDestroy, OnInit {

    public messages: Message[] = [];
    destroyed$ = new Subject<boolean>();
    messages$$ = new Subject<LogMessage>();
    messages$: Observable<LogMessage> = this.messages$$.asObservable();
    restoredLogs: LogMessage[] = [];


    constructor(public ls: RemoteLogService,
                public date: DatePipe,
                public length: LenghtPipe
    ) {

    }


    ngOnInit() {
        this.ls.connect()
            .pipe(
                takeUntil(this.destroyed$)
            )
            .subscribe(message => {
                this.addMessage(message);
            });
    }


    addMessage(message: Message) {
        console.log(message)
        /*if (this.messages.length > 100) {
            this.messages.shift()
        }
        this.messages.push(message);*/

        //{date:HH:mm:ss.SSS} [{thread|size=30}] {level|size=5} {class|size=50}::{method|size=35} - {message}

        let className = message.className;
        if (className && className.length > 10) {
            let parts = className.split(".");
            const last = parts.pop()
            className = parts.map(v => v.substring(0, 1)).join(".") + "." + last
        }
        let m = "[" + message.thread?.name + "] " + this.length.transform(message.level, 5) + " " + className + "::" + message.methodName + " - " + message.message;

        let value: LogMessage = {
            message: m,
            timestamp: this.date.transform(message.timestamp, 'medium') || "",
            type: message.level as any
        };
        this.messages$$.next(value);
    }

    ngOnDestroy(): void {
        this.destroyed$.next(true);
    }
}

interface Message {
    message: string
    className?: string
    context?: string
    exception?: string
    fileName?: string
    level?: string
    lineNumber?: string
    methodName?: string
    tag?: string
    thread?: {
        name: string
    }
    timestamp?: Date
}
