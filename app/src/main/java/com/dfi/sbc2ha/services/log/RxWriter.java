package com.dfi.sbc2ha.services.log;


import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

/**
 * This writer does nothing. It is used in the tinylog configuration to prevent JVM optimizations at runtime that would
 * prevent reconfiguring tinylog.
 */
public class RxWriter implements Writer {

    private final RxLogPublisher instance;


    public RxWriter(Map<String, String> properties) {
        instance = RxLogPublisher.getInstance();
    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        EnumSet<LogEntryValue> logEntryValues = EnumSet.allOf(LogEntryValue.class);
        logEntryValues.remove(LogEntryValue.CONTEXT);
        logEntryValues.remove(LogEntryValue.EXCEPTION);
        return logEntryValues;
        //return Collections.emptySet();
    }

    @Override
    public void write(LogEntry logEntry) {
        // Nothing to do
        instance.publish(logEntry);
    }

    @Override
    public void flush() {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to do
    }

}