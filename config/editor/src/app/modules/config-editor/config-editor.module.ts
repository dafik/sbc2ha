import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EditComponent} from './component/edit/edit.component';
import {CreatorComponent} from './component/creator/creator.component';
import {ConfigEditorRoutingModule} from "./config-editor-routing.module";
import {MatButtonModule} from "@angular/material/button";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {ReactiveFormsModule} from "@angular/forms";
import {MatStepperModule} from "@angular/material/stepper";
import {MatSelectModule} from "@angular/material/select";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatIconModule} from "@angular/material/icon";
import {PlatformListComponent} from './component/platform/platform-list/platform-list.component';
import {MatListModule} from "@angular/material/list";
import {PlatformAddComponent} from './component/platform/platform-add/platform-add.component';
import {MatDialogModule} from "@angular/material/dialog";
import {PlatformMqttComponent} from './component/config/platform/platform-mqtt/platform-mqtt.component';
import {PlatformOledComponent} from './component/config/platform/platform-oled/platform-oled.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {PlatformBusComponent} from './component/config/platform/platform-bus/platform-bus.component';
import {PlatformI2cComponent} from './component/config/platform/platform-i2c/platform-i2c.component';
import {PlatformModbusComponent} from './component/config/platform/platform-modbus/platform-modbus.component';
import {SensorListComponent} from './component/sensor/sensor-list/sensor-list.component';
import {SensorAddComponent} from './component/sensor/sensor-add/sensor-add.component';
import {ActuatorListComponent} from './component/actuator/actuator-list/actuator-list.component';
import {ActuatorAddComponent} from './component/actuator/actuator-add/actuator-add.component';
import {SensorAnalogComponent} from './component/config/sensor/sensor-analog/sensor-analog.component';
import {FilterListComponent} from './component/config/sensor/filter/filer-list/filter-list.component';
import {FilterAddComponent} from './component/config/sensor/filter/filter-add/filter-add.component';
import {SensorResistanceComponent} from './component/config/sensor/sensor-resistance/sensor-resistance.component';
import {SensorNtcComponent} from './component/config/sensor/sensor-ntc/sensor-ntc.component';
import {MatRadioModule} from "@angular/material/radio";
import {SensorDigitalComponent} from './component/config/sensor/sensor-digital/sensor-digital.component';
import {SensorDs2482Component} from './component/config/sensor/sensor-ds2482/sensor-ds2482.component';
import {SensorDallasComponent} from './component/config/sensor/sensor-dallas/sensor-dallas.component';
import {SensorLm75Component} from './component/config/sensor/sensor-lm75/sensor-lm75.component';
import {SensorModbusComponent} from './component/config/sensor/sensor-modbus/sensor-modbus.component';
import {SensorSwitchComponent} from './component/config/sensor/sensor-switch/sensor-switch.component';
import {ActuatorMcpComponent} from './component/config/actuator/actuator-mcp/actuator-mcp.component';
import {ActuatorPcaComponent} from './component/config/actuator/actuator-pca/actuator-pca.component';
import {ActuatorGpioComponent} from './component/config/actuator/actuator-gpio/actuator-gpio.component';
import {ActuatorGpiopwmComponent} from './component/config/actuator/actuator-gpiopwm/actuator-gpiopwm.component';
import {ActuatorCoverComponent} from './component/config/actuator/actuator-cover/actuator-cover.component';
import {JsonDPipe} from './json-d.pipe';
import {
    ActionsDigitalListComponent
} from './component/config/sensor/actions/digital/actions-digital-list/actions-digital-list.component';
import {
    ActionsSwitchListComponent
} from './component/config/sensor/actions/switch/actions-switch-list/actions-switch-list.component';
import {ActionsAddTypeComponent} from './component/config/sensor/actions/actions-add-type/actions-add-type.component';
import {ActionsAddComponent} from './component/config/sensor/actions/actions-add/actions-add.component';
import {ActionMqttComponent} from './component/config/sensor/actions/action-mqtt/action-mqtt.component';
import {ActionOutputComponent} from './component/config/sensor/actions/action-output/action-output.component';
import {ActionCoverComponent} from './component/config/sensor/actions/action-cover/action-cover.component';
import {HexPipe} from './hex.pipe';
import {MatInputHexDirective} from './mat-input-hex.directive';
import {MatCardModule} from "@angular/material/card";
import {YamlPipe} from './yaml.pipe';
import { LogListComponent } from './component/log/log-list/log-list.component';
import { LogAddDefaultComponent } from './component/log/log-add-default/log-add-default.component';
import { LogAddWriterComponent } from './component/log/log-add-writer/log-add-writer.component';
import { LogAddPackageComponent } from './component/log/log-add-package/log-add-package.component';


@NgModule({
    declarations: [
        EditComponent,
        CreatorComponent,
        PlatformListComponent,
        PlatformAddComponent,
        PlatformMqttComponent,
        PlatformOledComponent,
        PlatformBusComponent,
        PlatformI2cComponent,
        PlatformModbusComponent,
        SensorListComponent,
        SensorAddComponent,
        ActuatorListComponent,
        ActuatorAddComponent,
        SensorAnalogComponent,
        FilterListComponent,
        FilterAddComponent,
        SensorResistanceComponent,
        SensorNtcComponent,
        SensorDigitalComponent,
        SensorDs2482Component,
        SensorDallasComponent,
        SensorLm75Component,
        SensorModbusComponent,
        SensorSwitchComponent,
        ActuatorMcpComponent,
        ActuatorPcaComponent,
        ActuatorGpioComponent,
        ActuatorGpiopwmComponent,
        ActuatorCoverComponent,
        JsonDPipe,
        ActionsDigitalListComponent,
        ActionsSwitchListComponent,
        ActionsAddTypeComponent,
        ActionsAddComponent,
        ActionMqttComponent,
        ActionOutputComponent,
        ActionCoverComponent,
        HexPipe,
        MatInputHexDirective,
        YamlPipe,
        LogListComponent,
        LogAddDefaultComponent,
        LogAddWriterComponent,
        LogAddPackageComponent
    ],
    imports: [
        CommonModule,
        ConfigEditorRoutingModule,
        MatStepperModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatSelectModule,
        MatButtonModule,
        MatExpansionModule,
        MatIconModule,
        MatListModule,
        MatDialogModule,
        MatCheckboxModule,
        MatRadioModule,
        MatCardModule
    ],
    providers: [YamlPipe, JsonDPipe]
})
export class ConfigEditorModule {
}
