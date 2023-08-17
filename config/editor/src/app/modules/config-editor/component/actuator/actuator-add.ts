import {ComponentType} from "@angular/cdk/portal";
import {OutputKindType} from "../../../../definition/enums/OutputKindType";
import {McpOutputConfig} from "../../../../definition/actuator/McpOutputConfig";
import {ActuatorMcpComponent} from "../config/actuator/actuator-mcp/actuator-mcp.component";
import {ActuatorPcaComponent} from "../config/actuator/actuator-pca/actuator-pca.component";
import {PcaOutputConfig} from "../../../../definition/actuator/PcaOutputConfig";
import {GpioOutputConfig} from "../../../../definition/actuator/GpioOutputConfig";
import {CoverConfig} from "../../../../definition/actuator/CoverConfig";
import {ActuatorGpioComponent} from "../config/actuator/actuator-gpio/actuator-gpio.component";
import {ActuatorGpiopwmComponent} from "../config/actuator/actuator-gpiopwm/actuator-gpiopwm.component";
import {ActuatorCoverComponent} from "../config/actuator/actuator-cover/actuator-cover.component";

export const selectableActuatorDefinition: SelectableActuatorItem[] = [
  {
    kind: OutputKindType.MCP,
    component: ActuatorMcpComponent,
    config: McpOutputConfig,
  },
  {
    kind: OutputKindType.PCA,
    component: ActuatorPcaComponent,
    config: PcaOutputConfig,
  },
  {
    kind: OutputKindType.GPIO,
    component: ActuatorGpioComponent,
    config: GpioOutputConfig,
  },
  {
    kind: OutputKindType.GPIOPWM,
    component: ActuatorGpiopwmComponent,
    config: GpioOutputConfig,
  },
  {
    kind: OutputKindType.COVER,
    component: ActuatorCoverComponent,
    config: CoverConfig,
  },

];

export interface SelectableActuatorItem {
  kind: OutputKindType
  component: ComponentType<any>
  config: Object
}
