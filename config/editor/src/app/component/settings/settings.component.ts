import {Component} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {


  constructor(private http: HttpClient, private router: Router) {
  }

  reload() {
    this.http.get("/api/reload").subscribe(value => {
      this.router.navigate(['/logs']);
    })

  }

  restart() {
    this.http.get("/api/restart").subscribe(value => {
      this.router.navigate(['/logs']);
    })
  }

  stop() {
    this.http.get("/api/stop").subscribe()
  }

  clearStateCache() {
    this.http.get("/api/clear/states").subscribe()
  }

  clearConfigCache() {
    this.http.get("/api/clear/config").subscribe()
  }


}
