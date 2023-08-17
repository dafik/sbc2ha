import {ExtensionBoardsConfig} from "./extentsionBoard/ExtensionBoardsConfig";
import {PlatformConfig} from "./platform/PlatformConfig";
import {SensorConfig} from "./sensor/SensorConfig";
import {ActuatorConfig} from "./actuator/ActuatorConfig";
import {LoggerConfig} from "./LoggerConfig";


export class AppConfig {
    extensionBoards?: ExtensionBoardsConfig ;
    platform: PlatformConfig[] = [];
    sensor: SensorConfig[] = [];
    actuator: ActuatorConfig[] = [];
    logger?: LoggerConfig ;
}
