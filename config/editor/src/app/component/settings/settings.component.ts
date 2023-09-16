import {Component} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {environment} from "../../../environments/environment";

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {


    constructor(private http: HttpClient, private router: Router, public snackBar: MatSnackBar) {
    }

    reload() {
        if (environment.webOnly!) {
            this.webOnly();
        } else {
            this.http.get("/api/reload").subscribe(value => {
                this.router.navigate(['/logs']);
            })
        }

    }

    restart() {
        if (environment.webOnly!) {
            this.webOnly();
        } else if (environment.site!) {
            this.site();
        } else {
            this.http.get("/api/restart").subscribe(value => {
                this.router.navigate(['/logs']);
            })
        }
    }

    stop() {
        if (environment.webOnly!) {
            this.webOnly();
        } else if (environment.site!) {
            this.site();
        } else {
            this.http.get("/api/stop").subscribe()
        }
    }

    clearStateCache() {
        if (environment.webOnly!) {
            this.webOnly();
        } else {
            this.http.get("/api/clear/states").subscribe()
        }
    }

    clearConfigCache() {
        if (environment.webOnly!) {
            this.webOnly();
        } else {
            this.http.get("/api/clear/config").subscribe()
        }
    }

    webOnly() {
        this.snackBar.open("unavailable in webOnly preview", "Ok, i understand", {panelClass: 'error'})
    }

    site() {
        this.snackBar.open("unavailable in docker", "Ok, i understand", {panelClass: 'error'})
    }
}
