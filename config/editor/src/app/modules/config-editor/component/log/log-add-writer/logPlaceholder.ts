export enum LogPlaceholder {
    CLOSING_CURLY_BRACKET = 'closing_curly_bracket',
    CLASS = 'class',
    CLASS_NAME = 'class_name',
    DATE = 'date',
    EXCEPTION = 'exception',
    FILE = 'file',
    LEVEL = 'level',
    LEVEL_CODE = 'level_code',
    LINE = 'line',
    MESSAGE = 'message',
    MESSAGE_ONLY = 'message_only',
    METHOD = 'method',
    OPENING_CURLY_BRACKET = 'opening_curly_bracket',
    PACKAGE = 'package',
    PID = 'pid',
    PIPE = 'pipe',
    TAG = 'tag',
    THREAD = 'thread',
    THREAD_ID = 'thread_id',
    TIMESTAMP = 'timestamp',
    UPTIME = 'uptime',

}

export interface LogPlaceholderData {
    value: string;
    description: string;
}

export const logPlaceholderData: Record<LogPlaceholder, LogPlaceholderData> = {
    [LogPlaceholder.CLOSING_CURLY_BRACKET]: {
        value: "{closing-curly-bracket}",
        description: "Closing curly bracket: \"}\""
    },
    [LogPlaceholder.CLASS]: {
        value: "{class}",
        description: "Fully-qualified name of the class in which the log entry was issued"
    },
    [LogPlaceholder.CLASS_NAME]: {
        value: "{class-name}",
        description: "Name of the class (without package) in which the log entry was issued"
    },
    [LogPlaceholder.DATE]: {
        value: "{date}",
        description: "Date and time of issuing the log entry. Optionally there can be a custom date format pattern such as {date: HH:mm:ss.SSS}. The date format pattern is compatible with SimpleDateFormat and on Java 9 (or higher), also with DateTimeFormatter that supports milliseconds and nanoseconds. The default date format pattern is “yyyy-MM-dd HH:mm:ss”."
    },
    [LogPlaceholder.EXCEPTION]: {
        value: "{exception}",
        description: "Logged exception including stack trace"
    },
    [LogPlaceholder.FILE]: {
        value: "{file}",
        description: "Filename of the source file in which the log entry was issued"
    },
    [LogPlaceholder.LEVEL]: {
        value: "{level}",
        description: "Severity level of the log entry"
    },
    [LogPlaceholder.LEVEL_CODE]: {
        value: "{level-code}",
        description: "Numeric code of the severity level of the log entry (“1” for error … “5” for trace)"
    },
    [LogPlaceholder.LINE]: {
        value: "{line}",
        description: "Line number of the source file in which the log entry was issued"
    },
    [LogPlaceholder.MESSAGE]: {
        value: "{message}",
        description: "Logged message including exception and stack trace if present"
    },
    [LogPlaceholder.MESSAGE_ONLY]: {
        value: "{message-only}",
        description: "Only logged message without exception and stack trace"
    },
    [LogPlaceholder.METHOD]: {
        value: "{method}",
        description: "Name of the method in which the log entry was issued"
    },
    [LogPlaceholder.OPENING_CURLY_BRACKET]: {
        value: "{opening-curly-bracket}",
        description: "Opening curly bracket: “{”"
    },
    [LogPlaceholder.PACKAGE]: {
        value: "{package}",
        description: "Package in which the log entry was issued"
    },
    [LogPlaceholder.PID]: {
        value: "{pid}",
        description: "Process ID of the application"
    },
    [LogPlaceholder.PIPE]: {
        value: "{pipe}",
        description: "Pipe / vertical bar: “|”"
    },
    [LogPlaceholder.TAG]: {
        value: "{tag}",
        description: "Tag of log entry. By default, nothing will be output for untagged log entries. However, a default text can be output if explicitly configured, as in {tag: none} for example."
    },
    [LogPlaceholder.THREAD]: {
        value: "{thread}",
        description: "Name of the thread in which the log entry was issued"
    },
    [LogPlaceholder.THREAD_ID]: {
        value: "{thread-id}",
        description: "ID of the thread in which the log entry was issued"
    },
    [LogPlaceholder.TIMESTAMP]: {
        value: "{timestamp}",
        description: "UNIX timestamp of issuing the log entry. By default, the timestamp is output in seconds. {timestamp: milliseconds} outputs the timestamp in milliseconds."
    },
    [LogPlaceholder.UPTIME]: {
        value: "{uptime}",
        description: `
        Application’s uptime when the log entry was issued. The default format pattern is “HH:mm:ss”. It is also possible to define a custom format pattern such as {uptime: d:HH:mm:ss.SSS}. Supported tokens are “d” for days, “H” for hours, “m” for minutes, “s” for seconds, and “S” for fraction of second. Days are defined as 24 hours, even on days with time change.
        Text can be escaped by using single quotes like in {uptime: H'h'mm}. The highest defined time unit is never cut off. For example, 48h00 will be output after 48 hours of uptime in the previous example.
        Unlike standard Java, Android does not provide an API to get the application’s uptime. Instead, tinylog outputs the time difference between the first and the current log entry on Android. It is therefore recommended to issue a log entry as one of the first statements in onCreate() in the main activity. This can even be a trace log entry that is never output due to the actual severity level configuration.`
    },
}
