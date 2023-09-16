package com.dfi.sbc2ha.components.sensor.binary.click.timer;

import java.util.TimerTask;
import java.util.function.LongConsumer;

public class ClickTimerTask {

    private final LongConsumer task;
    private final int delay;
    private TimerTask timer;
    private TimerTaskState state = TimerTaskState.VIRGIN;

    public ClickTimerTask(LongConsumer task, int delay) {
        this.task = task;
        this.delay = delay;
    }

    public void schedule() {
        reset();
        timer = new TimerTask() {
            @Override
            public void run() {
                state = TimerTaskState.EXECUTED;
                long value = System.currentTimeMillis();
                long value1 = System.nanoTime();
                task.accept(value);
            }
        };
        ClickTimer instance = ClickTimer.getInstance();
        instance.schedule(timer, delay);
        state = TimerTaskState.SCHEDULED;
    }

    public void cancel() {
        if (timer.cancel()) {
            state = TimerTaskState.CANCELLED;
        }
    }

    public void reset() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        state = TimerTaskState.VIRGIN;
    }

    public boolean isRunning() {
        return state == TimerTaskState.SCHEDULED;
    }

    public TimerTaskState getState() {
        return state;
    }

    public enum TimerTaskState {
        /**
         * This task has not yet been scheduled.
         */
        VIRGIN,

        /**
         * This task is scheduled for execution.  If it is a non-repeating task,
         * it has not yet been executed.
         */
        SCHEDULED,

        /**
         * This non-repeating task has already executed (or is currently
         * executing) and has not been cancelled.
         */
        EXECUTED,

        /**
         * This task has been cancelled (with a call to TimerTask.cancel).
         */
        CANCELLED
    }
}
