import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {editorRoutingPaths} from "../../shared/app-routing-paths.config";
import {EditComponent} from "./component/edit/edit.component";
import {CreatorComponent} from "./component/creator/creator.component";


const routes: Routes = [
  {
    path: '',
    component: EditComponent
  },

  {
    path: editorRoutingPaths.creator.path,
    component: CreatorComponent
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigEditorRoutingModule {
}
