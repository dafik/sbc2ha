import {NgModule, isDevMode} from '@angular/core';
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
import {DatePipe, NgOptimizedImage} from "@angular/common";
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
import { LenghtPipe } from './shared/lenght.pipe';
import {MatSnackBarModule} from "@angular/material/snack-bar";
import { StoreModule } from '@ngrx/store';
import { reducers, metaReducers } from './reducers';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { EffectsModule } from '@ngrx/effects';
import { AppEffects } from './app.effects';
import { Nav1Component } from './component/nav1/nav1.component';
import { MatSidenavModule } from '@angular/material/sidenav';


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
        Nav1Component,
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
        StoreModule.forRoot(reducers, { metaReducers }),
        StoreDevtoolsModule.instrument(),
        EffectsModule.forRoot([AppEffects]),
        MatSidenavModule
    ],
    providers: [DatePipe,LenghtPipe],
    bootstrap: [LayoutComponent]
})
export class AppModule {
}
