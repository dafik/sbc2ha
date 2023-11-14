import {ModuleWithProviders, NgModule, Type} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HomeComponent} from './component/home/home.component';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatCardModule} from '@angular/material/card';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {ToolbarComponent} from './component/common/toolbar/toolbar.component';
import {APP_BASE_HREF, DatePipe, NgOptimizedImage} from "@angular/common";
import {LayoutComponent} from './component/common/layout/layout.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import {HttpClientModule} from "@angular/common/http";
import {ConfigComponent} from './component/config/config.component';
import {StatesComponent} from './component/states/states.component';
import {LogsComponent} from './component/logs/logs.component';
import {SettingsComponent} from './component/settings/settings.component';
import {MatListModule} from "@angular/material/list";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {LogMonitorModule} from "./modules/log-monitor/log-monitor.module";
import {LenghtPipe} from './shared/lenght.pipe';
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {StoreModule} from '@ngrx/store';
import {metaReducers, reducers} from './reducers';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {EffectsModule} from '@ngrx/effects';
import {AppEffects} from './app.effects';
import {MatSidenavModule} from '@angular/material/sidenav';
import {WebOnlyComponent} from './component/common/web-only/web-only.component';
import {MatDialogModule} from "@angular/material/dialog";
import {FormsModule} from "@angular/forms";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {MatTooltipModule} from "@angular/material/tooltip";
import {NgxGoogleAnalyticsModule, NgxGoogleAnalyticsRouterModule} from "ngx-google-analytics";
import {environment} from "../environments/environment";

let siteModules:Array<Type<any> | ModuleWithProviders<{}> | any[]> =[];

if (environment.site && environment.ga) {
    siteModules = [
        NgxGoogleAnalyticsModule.forRoot(environment.ga),
        NgxGoogleAnalyticsRouterModule
    ];
}

@NgModule({
    declarations: [
        HomeComponent,
        ToolbarComponent,
        LayoutComponent,
        ConfigComponent,
        StatesComponent,
        LogsComponent,
        SettingsComponent,
        LenghtPipe,
        WebOnlyComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MatGridListModule,
        MatCardModule,
        MatMenuModule,
        MatIconModule,
        MatButtonModule,
        NgOptimizedImage,
        MatToolbarModule,
        MatButtonModule,
        HttpClientModule,
        MatListModule,
        MatSlideToggleModule,
        LogMonitorModule,
        MatSnackBarModule,
        MatDialogModule,
        StoreModule.forRoot(reducers, {metaReducers}),
        StoreDevtoolsModule.instrument(),
        EffectsModule.forRoot([AppEffects]),
        MatSidenavModule,
        FormsModule,
        MatCheckboxModule,
        MatFormFieldModule,
        MatInputModule,
        MatOptionModule,
        MatSelectModule,
        MatTooltipModule,
        ...siteModules
    ],
    providers: [
        DatePipe,
        LenghtPipe,
    ],
    bootstrap: [LayoutComponent]
})
export class AppModule {
}
