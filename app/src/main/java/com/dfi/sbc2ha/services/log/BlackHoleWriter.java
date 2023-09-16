package com.dfi.sbc2ha.services.log;


import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * This writer does nothing. It is used in the tinylog configuration to prevent JVM optimizations at runtime that would
 * prevent reconfiguring tinylog.
 */
public class BlackHoleWriter implements Writer {

    public BlackHoleWriter(Map<String, String> properties) {
    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return Collections.emptySet();
    }

    @Override
    public void write(LogEntry logEntry) {
        // Nothing to do
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