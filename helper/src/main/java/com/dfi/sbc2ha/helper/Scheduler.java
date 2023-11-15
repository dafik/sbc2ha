package com.dfi.sbc2ha.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Scheduler {
    private static Scheduler instance;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor;
    private final SbcThreadFactory threadFactory;

    private Scheduler() {
        threadFactory = new SbcThreadFactory();
        scheduler = Executors.newScheduledThreadPool(0, threadFactory);
        // Note pool size is 0 and keepAliveTime is 0 to prevent shutdown delays
        executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
    }

    /**
     * Get the diozero scheduler instance that exclusively uses non-daemon threads
     * (aka user threads).
     * <p>
     * Non-daemon / user threads are high-priority threads (in the context of the
     * JVM). The JVM will wait for any user thread to complete its task before
     * terminating.
     *
     * @return the diozero scheduler instance that uses daemon threads
     */
    public static synchronized Scheduler getInstance() {
        if (instance == null || instance.isShutdown()) {
            instance = new Scheduler();
        }
        return instance;
    }

    public static void shutdownAll() {
        if (instance != null) {
            instance.shutdown();
        }
    }

    public static void statusAll() {
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
            log.debug("Stack trace elements for Thread " + entry.getKey().getName() + ":");
            for (StackTraceElement element : entry.getValue()) {
                log.debug(element.toString());
            }
        }
        instance.status();
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }


    private void shutdown() {
        scheduler.shutdownNow();
        executor.shutdownNow();
        log.trace("Shutdown - done");
    }

    public boolean isShutdown() {
        return scheduler.isShutdown() && executor.isShutdown();
    }

    private void status() {
        threadFactory.status();
    }

    static class SbcThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        SbcThreadFactory() {

            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "sbc2ha-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            return t;
        }

        void status() {
            log.debug("activeCount=" + group.activeCount()
                    + ", activeGroupCount=" + group.activeGroupCount());
        }
    }
}
