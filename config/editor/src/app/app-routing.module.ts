import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {appRoutingPaths} from "./shared/app-routing-paths.config";
import {HomeComponent} from "./component/home/home.component";
import {StatesComponent} from "./component/states/states.component";
import {SettingsComponent} from "./component/settings/settings.component";
import {LogsComponent} from "./component/logs/logs.component";
import {ConfigComponent} from "./component/config/config.component";
import {environment} from "../environments/environment";

const routes: Routes = [
  {
    path: appRoutingPaths['editor'].path,
    loadChildren: () =>
      import('src/app/modules/config-editor/config-editor.module').then(
        (mod) => mod.ConfigEditorModule
      ),
  },
  {
    path: appRoutingPaths['home'].path,
    component: HomeComponent
  },
  {
    path: appRoutingPaths['states'].path,
    component: StatesComponent
  },
  {
    path: appRoutingPaths['settings'].path,
    component: SettingsComponent
  },
  {
    path: appRoutingPaths['logs'].path,
    component: LogsComponent
  },
  {
    path: appRoutingPaths['config'].path,
    component: ConfigComponent
  },
  {path: '**', redirectTo: `/${appRoutingPaths['home'].path}`},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, (
    {useHash: environment.useHash}
  ))],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
