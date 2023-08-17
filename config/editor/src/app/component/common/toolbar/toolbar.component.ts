import {Component} from '@angular/core';
import {appRoutingPaths} from "../../../shared/app-routing-paths.config";

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],

})
export class ToolbarComponent {

    protected readonly appRoutingPaths = appRoutingPaths;
}
