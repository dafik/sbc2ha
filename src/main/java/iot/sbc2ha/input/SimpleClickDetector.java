package iot.sbc2ha.input;

import iot.sbc2ha.device.ClicksConfig;
import iot.sbc2ha.runtime.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Click detector that distinguishes click, double-click, and long-press
 * based on the configured {@link ClicksConfig}.
 *
 * <h3>Detection modes (from ClicksConfig)</h3>
 * <ul>
 *   <li><b>CLICK_ONLY</b> — click=true, dbl=false, long=false
 *       → fires CLICK immediately on press (no waiting)</li>
 *   <li><b>CLICK+DOUBLE</b> — click=true, dbl=true, long=false
 *       → DOUBLE on second press within window; CLICK on release or timer expiry</li>
 *   <li><b>FULL</b> — click=true, dbl=true, long=true
 *       → CLICK/DOUBLE on release/timer; LONG+RELEASE on long press</li>
 * </ul>
 *
 * <h3>Timing constants (from old app)</h3>
 * <table>
 *   <tr><td>DOUBLE_CLICK_WINDOW_MS</td><td>350ms — two presses within this → DOUBLE</td></tr>
 *   <tr><td>LONG_PRESS_THRESHOLD_MS</td><td>700ms — held beyond this → LONG</td></tr>
 * </table>
 */
public final class SimpleClickDetector implements ClickDetector {

    private static final Logger log = LoggerFactory.getLogger(SimpleClickDetector.class);

    /** Double-click window: two presses within this → DOUBLE. From old app. */
    static final long DOUBLE_CLICK_WINDOW_MS = 350;

    /** Long-press threshold: held beyond this → LONG. From old app. */
    static final long LONG_PRESS_THRESHOLD_MS = 700;

    private final ScheduledExecutorService timerService;
    private final boolean ownsScheduler;
    private final Consumer<EventType> callback;
    private final ClicksConfig config;

    /** Press/release cycle state */
    enum CycleState {
        IDLE,           // no press active
        CLICK_WAIT,     // first press active, waiting for double/long or release
        LONG_WAIT,      // long press active (LONG fired), waiting for RELEASE
    }

    private CycleState state = CycleState.IDLE;
    private long pressStartNanos = 0;
    private boolean doubleFired = false;

    /** Double-click timer — fires CLICK if no second press within window */
    private ScheduledFuture<?> doubleClickTimer;

    /** Long-press timer — fires LONG if held past threshold */
    private ScheduledFuture<?> longPressTimer;

    /**
     * Creates a detector using the provided shared scheduler.
     *
     * <p>The caller owns the scheduler lifecycle — do not call {@code shutdown()}
     * on the scheduler here. This is the preferred constructor for targets with
     * many input channels (e.g., BBB with 50 switches).</p>
     *
     * @param timerService shared scheduler (caller owns lifecycle)
     * @param config click detection settings
     * @param callback the consumer that receives EventType events
     */
    public SimpleClickDetector(ScheduledExecutorService timerService, ClicksConfig config, Consumer<EventType> callback) {
        this(timerService, false, config, callback);
    }

    /** Internal shared constructor. */
    private SimpleClickDetector(ScheduledExecutorService timerService, boolean ownsScheduler,
                                ClicksConfig config, Consumer<EventType> callback) {
        this.timerService = timerService;
        this.ownsScheduler = ownsScheduler;
        this.config = config;
        this.callback = callback;
    }

    @Override
    public synchronized void onPress(long timestamp) {
        switch (state) {
            case IDLE -> onPressFirst(timestamp);
            case CLICK_WAIT -> onPressSecond(timestamp);
            case LONG_WAIT -> {
                // New press starts after LONG+RELEASE
                cancelTimers();
                state = CycleState.IDLE;
                onPressFirst(timestamp);
            }
        }
    }

    /** First press of a cycle — fire immediately (CLICK_ONLY) or start timers. */
    private void onPressFirst(long timestamp) {
        cancelTimers();
        pressStartNanos = timestamp;
        doubleFired = false;

        if (!config.click() && !config.dbl() && !config.longPress()) {
            return;
        }

        // CLICK_ONLY mode: fire immediately on press, no waiting
        if (config.click() && !config.dbl() && !config.longPress()) {
            callback.accept(EventType.CLICK);
            log.debug("CLICK_ONLY: fired CLICK immediately on press at t={}", timestamp);
            return;
        }

        // CLICK+DOUBLE or FULL: enter CLICK_WAIT, start timers
        state = CycleState.CLICK_WAIT;

        // Long-press timer — fires if held past threshold
        if (config.longPress()) {
            longPressTimer = timerService.schedule(() -> {
                synchronized (SimpleClickDetector.this) {
                    if (state == CycleState.CLICK_WAIT) {
                        cancelTimers();
                        state = CycleState.LONG_WAIT;
                        doubleFired = false;  // Reset for RELEASE firing
                        callback.accept(EventType.LONG);
                        log.info("Long press detected at t={}", timestamp);
                    }
                }
            }, LONG_PRESS_THRESHOLD_MS, TimeUnit.MILLISECONDS);
        } else {
            // Double-click timer — fires CLICK if no second press within window
            // (dbl is always true here: CLICK_ONLY mode returned above, longPress is false)
            doubleClickTimer = timerService.schedule(() -> {
                synchronized (SimpleClickDetector.this) {
                    if (state == CycleState.CLICK_WAIT) {
                        cancelTimers();
                        state = CycleState.IDLE;
                        callback.accept(EventType.CLICK);
                        log.debug("Double-click window expired, fired CLICK at t={}", timestamp);
                    }
                }
            }, DOUBLE_CLICK_WINDOW_MS, TimeUnit.MILLISECONDS);
        }
    }

    /** Second press within window — fire DOUBLE immediately. */
    @SuppressWarnings("ConstantConditions") // dbl can be false when longPress=true
    private void onPressSecond(long timestamp) {
        cancelTimers();
        if (config.dbl()) {
            doubleFired = true;
            callback.accept(EventType.DOUBLE);
            log.debug("Second press within window at t={}, fired DOUBLE", timestamp);
            // Start new cycle for potential subsequent presses (double-double, etc.)
            state = CycleState.CLICK_WAIT;
            if (config.longPress()) {
                longPressTimer = timerService.schedule(() -> {
                    synchronized (SimpleClickDetector.this) {
                        if (state == CycleState.CLICK_WAIT) {
                            cancelTimers();
                            state = CycleState.LONG_WAIT;
                            callback.accept(EventType.LONG);
                            log.info("Long press after double at t={}", timestamp);
                        }
                    }
                }, LONG_PRESS_THRESHOLD_MS, TimeUnit.MILLISECONDS);
            } else {
                doubleClickTimer = timerService.schedule(() -> {
                    synchronized (SimpleClickDetector.this) {
                        if (state == CycleState.CLICK_WAIT) {
                            cancelTimers();
                            state = CycleState.IDLE;
                            callback.accept(EventType.CLICK);
                            log.debug("Double-click window expired, fired CLICK at t={}", timestamp);
                        }
                    }
                }, DOUBLE_CLICK_WINDOW_MS, TimeUnit.MILLISECONDS);
            }
        } else {
            state = CycleState.IDLE;
        }
    }

    @Override
    public synchronized void onRelease(long timestamp) {
        // CLICK_ONLY mode: nothing to do on release (event already fired on press)
        if (config.click() && !config.dbl() && !config.longPress()) {
            return;
        }

        switch (state) {
            case CLICK_WAIT -> {
                // Release before long-press timeout
                cancelTimers();
                state = CycleState.IDLE;
                // Only fire CLICK if DOUBLE wasn't already fired in this cycle
                if (config.click() && !doubleFired) {
                    callback.accept(EventType.CLICK);
                    log.debug("Short press release at t={}, fired CLICK", timestamp);
                }
            }
            case LONG_WAIT -> {
                // Long pressed → release fires RELEASE
                cancelTimers();
                state = CycleState.IDLE;
                if (config.release()) {
                    callback.accept(EventType.RELEASE);
                    log.debug("Long press release at t={}, fired RELEASE", timestamp);
                }
            }
            case IDLE -> {
                /* already done */
            }
        }
    }

    private void cancelTimers() {
        if (doubleClickTimer != null) {
            doubleClickTimer.cancel(true);
            doubleClickTimer = null;
        }
        if (longPressTimer != null) {
            longPressTimer.cancel(true);
            longPressTimer = null;
        }
    }

    /**
     * Shuts down the internal timer if this instance owns it.
     *
     * <p>Does nothing when the detector was created with a shared scheduler
     * — the caller must manage that scheduler's lifecycle separately.</p>
     */
    public void shutdown() {
        cancelTimers();
        if (ownsScheduler) {
            timerService.shutdown();
        }
    }

    @SuppressWarnings("unused")
    public boolean isPressActive() {
        return state != CycleState.IDLE;
    }

    @SuppressWarnings("unused")
    public long pressStartNanos() {
        return pressStartNanos;
    }

    @Override
    public String toString() {
        return "SimpleClickDetector{config=" + config + ", state=" + state + "}";
    }
}
