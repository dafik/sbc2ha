package iot.sbc2ha.input;

import iot.sbc2ha.runtime.EventType;

import java.util.function.Consumer;

/**
 * Simple fake click detector for testing.
 *
 * <p>Simulates a switch that produces a single event type when {@link #trigger()}
 * is called. Useful for unit tests that need to inject events into the
 * {@link iot.sbc2ha.runtime.ActionEngine} without real GPIO hardware.</p>
 */
public final class SimpleClickDetector implements ClickDetector {

    private final EventType eventType;
    private final Consumer<EventType> callback;

    /**
     * Creates a detector that fires the given event type on trigger.
     */
    public SimpleClickDetector(EventType eventType, Consumer<EventType> callback) {
        this.eventType = eventType;
        this.callback = callback;
    }

    /**
     * Creates a detector that fires the given event type when {@link #trigger()} is called.
     *
     * @param eventType      the event type to fire
     * @param callback       the consumer that receives the event type
     * @param debounceMillis debounce window in milliseconds (ignored in fake impl)
     */
    @SuppressWarnings("unused")
    public SimpleClickDetector(EventType eventType, Consumer<EventType> callback, long debounceMillis) {
        this(eventType, callback);
    }

    /**
     * Fires the configured event type via the callback.
     */
    public void trigger() {
        if (callback != null) {
            callback.accept(eventType);
        }
    }

    /**
     * @return the event type this detector fires
     */
    public EventType eventType() {
        return eventType;
    }

    // ClickDetector interface — no-op in fake impl

    @Override
    public void onPress(long timestampMonotonic) {
        // no-op
    }

    @Override
    public void onRelease(long timestampMonotonic) {
        // no-op
    }
}
