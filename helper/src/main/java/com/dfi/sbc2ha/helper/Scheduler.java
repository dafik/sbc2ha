package com.dfi.sbc2ha.helper;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     Scheduler.java
 *
 * This file is part of the diozero project. More information about this project
 * can be found at https://www.diozero.com/.
 * %%
 * Copyright (C) 2016 - 2023 diozero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.tinylog.Logger;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
            Logger.debug("Stack trace elements for Thread " + entry.getKey().getName() + ":");
            for (StackTraceElement element : entry.getValue()) {
                Logger.debug(element.toString());
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


    private void shutdown() {
        scheduler.shutdownNow();
        executor.shutdownNow();
        Logger.trace("Shutdown - done");
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
            Logger.debug("activeCount=" + group.activeCount()
                    + ", activeGroupCount=" + group.activeGroupCount());
        }
    }
}
