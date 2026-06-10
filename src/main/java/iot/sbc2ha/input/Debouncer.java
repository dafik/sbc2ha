package iot.sbc2ha.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Debouncer with a background timer thread.
 *
 * <p>Only fires events that are stable for at least {@code debounceMs}. Rapid
 * toggles (bounces) within the debounce window are suppressed. Duplicate
 * events for the same value are ignored.</p>
 *
 * <p>Uses a scheduled timer: each new value reschedules the timer. When the
 * timer fires, the event is forwarded to the callback.</p>
 *
 * <p>Use this version when you need the debouncer to fire events on its own
 * (e.g., for a real hardware adapter where no subsequent event triggers the
 * debounce expiry).</p>
 *
 * <p>Thread-safe — all state is accessed under a lock.</p>
 */
public final class Debouncer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Debouncer.class);

    private final ScheduledExecutorService timerService;
    private final boolean ownsScheduler;
    private final Consumer<Boolean> callback;
    private final long debounceMs;

    private boolean lastReportedValue;
    private boolean currentValue;
    private ScheduledFuture<?> timer;

    /**
     * Creates a debouncer that fires {@code callback} with the settled value
     * after {@code debounceMs} of stability, using its own internal scheduler.
     *
     * @param debounceMs debounce window in milliseconds
     * @param callback receives the settled value (true = pressed, false = released)
     */
    @SuppressWarnings("unused")
    public Debouncer(int debounceMs, Consumer<Boolean> callback) {
        this(createDefaultScheduler(), true, debounceMs, callback);
    }

    /**
     * Creates a debouncer that fires {@code callback} with the settled value
     * after {@code debounceMs} of stability, using the provided shared scheduler.
     *
     * <p>The caller owns the scheduler lifecycle — do not call {@code close()} on
     * the scheduler here. This is the preferred constructor for targets with many
     * input channels (e.g., BBB with 50 switches).</p>
     *
     * @param timerService shared scheduler (caller owns lifecycle)
     * @param debounceMs debounce window in milliseconds
     * @param callback receives the settled value (true = pressed, false = released)
     */
    public Debouncer(ScheduledExecutorService timerService, int debounceMs, Consumer<Boolean> callback) {
        this(timerService, false, debounceMs, callback);
    }

    /** Internal shared constructor. */
    private Debouncer(ScheduledExecutorService timerService, boolean ownsScheduler,
                      int debounceMs, Consumer<Boolean> callback) {
        this.timerService = timerService;
        this.ownsScheduler = ownsScheduler;
        this.callback = callback;
        this.debounceMs = debounceMs;
        this.currentValue = false;
        this.lastReportedValue = false;
        this.timer = null;
    }

    private static ScheduledExecutorService createDefaultScheduler() {
        return Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "debouncer");
                    t.setDaemon(true);
                    return t;
                });
    }

    /**
     * Called when a new state change is detected.
     *
     * @param value the new state (true = pressed, false = released)
     */
    public synchronized void accept(boolean value) {
        // Ignore duplicates — same value already pending or reported
        if (value == currentValue) {
            log.debug("Debouncer: ignoring duplicate value={}", value);
            return;
        }

        // Record new value and schedule/reschedule the debounce timer
        currentValue = value;
        scheduleTimer();
        log.debug("Debouncer: new value={}, debounce started", value);
    }

    private void scheduleTimer() {
        if (timer != null) {
            timer.cancel(false);
        }
        timer = timerService.schedule(this::fireIfStable, debounceMs, TimeUnit.MILLISECONDS);
    }

    private synchronized void fireIfStable() {
        if (currentValue != lastReportedValue) {
            callback.accept(currentValue);
            lastReportedValue = currentValue;
            log.debug("Debouncer: fired settled value={}", currentValue);
        }
        timer = null;
    }

    @Override
    public void close() {
        if (timer != null) {
            timer.cancel(false);
            timer = null;
        }
        if (ownsScheduler) {
            timerService.shutdown();
        }
    }
}
