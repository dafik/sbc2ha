package com.dfi.sbc2ha.manager;

import org.tinylog.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ManagerExecutor implements Runnable, AutoCloseable {
    private final BlockingQueue<ManagerCommand> queue = new LinkedBlockingQueue<>();
    private final Consumer<ManagerCommand> consumer;

    private final ManagerThreadFactory threadFactory;
    private final ExecutorService executor;
    private Future<?> queueFeature;

    public ManagerExecutor(Consumer<ManagerCommand> consumer) {
        this.consumer = consumer;
        threadFactory = new ManagerThreadFactory();
        executor = Executors.newFixedThreadPool(1, threadFactory);

        start();
    }

    public void addExternalCommand(ManagerCommandExternal command) {
        try {
            queue.put(command);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addInternalCommand(ManagerCommandInternal command) {
        try {
            queue.put(command);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void start() {
        queueFeature = executor.submit(this);
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public void run() {
        try {
            while (!queueFeature.isDone()) {
                ManagerCommand command = queue.take();
                consumer.accept(command);
            }

        } catch (InterruptedException e) {
            Logger.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        queueFeature.cancel(true);
        executor.shutdown();
    }

    static class ManagerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ManagerThreadFactory() {

            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "sbc2ha-manager-" + poolNumber.getAndIncrement() + "-thread-";
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
