import {Injectable, OnDestroy} from '@angular/core';
import {WebSocketSubject} from "rxjs/internal/observable/dom/WebSocketSubject";
import {delay, filter, map, Observable, of, retry, switchMap} from "rxjs";
import {webSocket} from "rxjs/webSocket";

@Injectable({
    providedIn: 'root'
})
export class RemoteLogService implements OnDestroy {

    connection$: WebSocketSubject<any> | null = null;
    RETRY_SECONDS = 1;

    connect(): Observable<any> {

        const protocol = window.location.protocol.replace('http', 'ws');
        const host = window.location.host;

        return of(`${protocol}//${host}`).pipe(
            filter(apiUrl => !!apiUrl),
            // https becomes wws, http becomes ws
            map(apiUrl => apiUrl.replace(/^http/, 'ws') + '/ws/logs'),
            switchMap(wsUrl => {
                if (this.connection$) {
                    return this.connection$;
                } else {
                    this.connection$ = webSocket(wsUrl);
                    return this.connection$;
                }
            }),
            retry({
                delay: (errors) => errors.pipe(delay(this.RETRY_SECONDS))
            })
        );
    }

    closeConnection() {
        if (this.connection$) {
            this.connection$.complete();
            this.connection$ = null;
        }
    }

    ngOnDestroy() {
        this.closeConnection();
    }
}
