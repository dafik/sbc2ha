package com.dfi.sbc2ha.manager;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
@Slf4j
public class ManagerExecutor implements Runnable, AutoCloseable {
    private final BlockingQueue<ManagerCommand> queue = new LinkedBlockingQueue<>();
    private final Consumer<ManagerCommand> consumer;

    private final ExecutorService executor;
    private Future<?> queueFeature;

    public ManagerExecutor(Consumer<ManagerCommand> consumer) {
        this.consumer = consumer;
        ManagerThreadFactory threadFactory = new ManagerThreadFactory();
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
            log.info("queue exit");

        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        queueFeature.cancel(true);
        executor.shutdown();
    }

    public static class ManagerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public ManagerThreadFactory() {

            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "sbc2ha-manager-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        }

        void status() {
            log.debug("activeCount=" + group.activeCount()
                    + ", activeGroupCount=" + group.activeGroupCount());
        }
    }
}
