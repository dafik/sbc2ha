import {LogLevel} from "./enums/LogLevel";
import {Type} from "class-transformer";

export class LoggerConfig {
    default?: LogLevel;

    @Type(() => Map<string,string>)
    logs?: Map<string, string>

    @Type(() => Map<string,string>)
    writer?: Map<string, string>
}
