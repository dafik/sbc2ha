package iot.sbc2ha.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Synchronous event debouncer for mechanical switch bounce filtering.
 *
 * <p>Only fires events that are stable for at least {@code debounceMs}. Rapid
 * toggles (bounces) within the debounce window are suppressed. Duplicate
 * events for the same value are ignored.</p>
 *
 * <p>Purely event-driven — the caller passes timestamps and the debouncer
 * decides immediately whether to fire. No background thread.</p>
 *
 * <h3>Algorithm (adapted from diozero)</h3>
 * <ol>
 *   <li>On each event, record the value and timestamp</li>
 *   <li>If the value differs from the last <em>reported</em> value, start
 *       tracking the debounce window</li>
 *   <li>When the next event arrives, check if the previous value was stable
 *       for at least {@code debounceMs}. If yes, fire it with its timestamp.</li>
 * </ol>
 *
 * <p>Use this version for testing or when the caller controls timing
 * (e.g., {@link ClickDetector} tests where timestamps are injected).</p>
 */
public final class SimpleDebouncer {

    private static final Logger log = LoggerFactory.getLogger(SimpleDebouncer.class);

    private final BiConsumer<Boolean, Long> callback;
    private final long debounceNanos;

    private boolean lastReportedValue;
    private boolean currentValue;
    private long changeTimeNanos;

    /**
     * Creates a debouncer that fires {@code callback} with the settled value
     * and its timestamp after {@code debounceMs} of stability.
     *
     * @param debounceMs debounce window in milliseconds
     * @param callback receives (value, timestampNanos) when a stable transition occurs
     */
    public SimpleDebouncer(int debounceMs, BiConsumer<Boolean, Long> callback) {
        this.callback = callback;
        this.debounceNanos = (long) debounceMs * 1_000_000L;
        this.currentValue = false;
        this.lastReportedValue = false;
        this.changeTimeNanos = 0;
    }

    /**
     * Called when a new state change is detected.
     *
     * @param value the new state (true = pressed, false = released)
     * @param timestampNanos current time in nanoseconds
     */
    public void accept(boolean value, long timestampNanos) {
        // Ignore duplicates — same value already pending or reported
        if (value == currentValue) {
            return;
        }

        // Check if previous value was stable enough to fire
        if (currentValue != lastReportedValue && timestampNanos - changeTimeNanos >= debounceNanos) {
            callback.accept(currentValue, changeTimeNanos);
            lastReportedValue = currentValue;
            log.debug("Debouncer: fired settled value={} at t={}ms",
                    currentValue, changeTimeNanos / 1_000_000L);
        }

        // Record new value
        currentValue = value;
        changeTimeNanos = timestampNanos;
        log.debug("Debouncer: new value={}, debounce started at t={}ms",
                value, timestampNanos / 1_000_000L);
    }

    /**
     * Forces the current pending value to fire if it has been stable for
     * at least {@code debounceMs}. Useful at the end of a test sequence or
     * when no further events are expected.
     *
     * @param timestampNanos current time in nanoseconds
     */
    public void flush(long timestampNanos) {
        if (currentValue != lastReportedValue && timestampNanos - changeTimeNanos >= debounceNanos) {
            callback.accept(currentValue, changeTimeNanos);
            lastReportedValue = currentValue;
            log.debug("Debouncer: flushed settled value={} at t={}ms",
                    currentValue, changeTimeNanos / 1_000_000L);
        }
    }
}
