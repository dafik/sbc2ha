import {LogLevel} from "./enums/LogLevel";

export interface LoggerConfig {
    default?: LogLevel;
    logs: Record<string, string>[];
    writer?: Record<string, string>[];
}
