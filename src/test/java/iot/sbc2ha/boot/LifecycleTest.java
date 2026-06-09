package iot.sbc2ha.boot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {

    private final ThrowingBootDisplay throwingDisplay = new ThrowingBootDisplay();
    private final LogRecordingBootDisplay logDisplay = new LogRecordingBootDisplay();

    @BeforeEach
    void setUp() {
        // Each test gets a fresh Lifecycle
    }

    @Test
    void initialStateIsBootting() {
        Lifecycle lifecycle = new Lifecycle(logDisplay);
        assertEquals(LifecycleState.BOOTING, lifecycle.state());
    }

    @Test
    void transitionUpdatesStateAndDisplay() {
        Lifecycle lifecycle = new Lifecycle(logDisplay);
        lifecycle.transition(LifecycleState.CONFIG_LOADED);

        assertEquals(LifecycleState.CONFIG_LOADED, lifecycle.state());
        assertEquals(1, logDisplay.updates.size());
        assertEquals(LifecycleState.CONFIG_LOADED, logDisplay.updates.getFirst());
    }

    @Test
    void transitionsChainCorrectly() {
        Lifecycle lifecycle = new Lifecycle(logDisplay);
        lifecycle.transition(LifecycleState.CONFIG_LOADED);
        lifecycle.transition(LifecycleState.STATE_RESTORED);
        lifecycle.transition(LifecycleState.OFFLINE_READY);

        assertEquals(LifecycleState.OFFLINE_READY, lifecycle.state());
        assertEquals(3, logDisplay.updates.size());
        assertEquals(LifecycleState.CONFIG_LOADED, logDisplay.updates.getFirst());
        assertEquals(LifecycleState.STATE_RESTORED, logDisplay.updates.get(1));
        assertEquals(LifecycleState.OFFLINE_READY, logDisplay.updates.getLast());
    }

    @Test
    void displayFailureDoesNotBlockTransition() {
        Lifecycle lifecycle = new Lifecycle(throwingDisplay);

        // First transition should log the warning but still succeed
        assertDoesNotThrow(() -> lifecycle.transition(LifecycleState.CONFIG_LOADED));
        assertEquals(LifecycleState.CONFIG_LOADED, lifecycle.state());

        // Second transition with same display should also succeed
        assertDoesNotThrow(() -> lifecycle.transition(LifecycleState.OFFLINE_READY));
        assertEquals(LifecycleState.OFFLINE_READY, lifecycle.state());
    }

    @Test
    void shutdownCallsDisplayClose() {
        LogRecordingBootDisplay display = new LogRecordingBootDisplay();
        Lifecycle lifecycle = new Lifecycle(display);

        assertDoesNotThrow(lifecycle::shutdown);
        assertTrue(display.closed);
    }

    @Test
    void shutdownDisplayFailureDoesNotThrow() {
        Lifecycle lifecycle = new Lifecycle(throwingDisplay);
        assertDoesNotThrow(lifecycle::shutdown);
    }

    /** {@link BootDisplay} that always throws on update. */
    private static class ThrowingBootDisplay implements BootDisplay {
        @Override
        public void update(LifecycleState state) {
            throw new RuntimeException("display is dead");
        }

        @Override
        public void close() {
            throw new RuntimeException("close failed");
        }
    }

    /** {@link BootDisplay} that records updates. */
    private static class LogRecordingBootDisplay implements BootDisplay {
        final java.util.List<LifecycleState> updates = new java.util.ArrayList<>();
        boolean closed = false;

        @Override
        public void update(LifecycleState state) {
            updates.add(state);
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
