package com.dfi.sbc2ha.log;

import org.tinylog.Level;
import org.tinylog.configuration.Configuration;
import org.tinylog.core.TinylogLoggingProvider;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.ContextProvider;
import org.tinylog.provider.LoggingProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This logging provider can be reconfigured at runtime.
 */
public class ReconfigurableLoggingProvider implements LoggingProvider {

    // Locking is required to ensure thread-safe configuration changes
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    // The real tinylog logging provider is used under the hood
    private static TinylogLoggingProvider realProvider = new TinylogLoggingProvider();

    public static void reload() throws InterruptedException, ReflectiveOperationException {
        lock.writeLock().lock();
        try {
            realProvider.shutdown();

            Field frozen = Configuration.class.getDeclaredField("frozen");
            frozen.setAccessible(true);
            frozen.setBoolean(null, false);

            Method method = Configuration.class.getDeclaredMethod("load");
            method.setAccessible(true);

            Map<String, String> configuration = (Map) method.invoke(null);
            Configuration.replace(configuration);

            realProvider = new TinylogLoggingProvider();

            frozen.setBoolean(null, true);
            method.setAccessible(false);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ContextProvider getContextProvider() {
        lock.readLock().lock();
        try {
            return realProvider.getContextProvider();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Level getMinimumLevel() {
        lock.readLock().lock();
        try {
            return realProvider.getMinimumLevel();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Level getMinimumLevel(final String tag) {
        lock.readLock().lock();
        try {
            return realProvider.getMinimumLevel(tag);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEnabled(final int depth, final String tag, final Level level) {
        lock.readLock().lock();
        try {
            return realProvider.isEnabled(depth + 1, tag, level);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void log(String loggerClassName, String tag, Level level, Throwable exception, MessageFormatter formatter, Object obj, Object... arguments) {
        lock.readLock().lock();
        try {
            realProvider.log(loggerClassName, tag, level, exception, formatter, obj, arguments);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void log(int depth, String tag, Level level, Throwable exception, MessageFormatter formatter, Object obj, Object... arguments) {
        lock.readLock().lock();
        try {
            realProvider.log(depth + 1, tag, level, exception, formatter, obj, arguments);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void shutdown() throws InterruptedException {
        lock.writeLock().lock();
        try {
            realProvider.shutdown();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
