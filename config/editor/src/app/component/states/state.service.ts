import {Injectable, OnDestroy} from '@angular/core';
import {WebSocketSubject} from "rxjs/internal/observable/dom/WebSocketSubject";
import {filter, map, Observable, of, retry, switchMap, timer} from "rxjs";
import {webSocket} from "rxjs/webSocket";
import {UrlHelper} from "../../shared/url-helper";

@Injectable({
    providedIn: 'root'
})
export class StateService implements OnDestroy {
    connection$: WebSocketSubject<any> | null = null;
    RETRY_SECONDS = 1;

    connect(): Observable<any> {

        return of(UrlHelper.getWsUrl()).pipe(
            filter(wsUrl => !!wsUrl),
            // https becomes wws, http becomes ws
            map(wsUrl => wsUrl + 'states'),
            switchMap(wsUrl => {
                if (this.connection$) {
                    return this.connection$;
                } else {
                    this.connection$ = webSocket(wsUrl);
                    return this.connection$;
                }
            }),

            retry({
                count: 3,
                delay: (error, count) => {
                    // Retry forever, but with an exponential step-back
                    // maxing out at 1 minute.
                    return timer(Math.min(60000, 2 ^ count * 1000))
                },
            })

            /*retry({
                delay: (errors) => {
                    return errors.pipe(delay(this.RETRY_SECONDS));
                }
            })*/
        );
    }

    send(message: any) {
        if (this.connection$) {
            this.connection$.next(message)
        }
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
