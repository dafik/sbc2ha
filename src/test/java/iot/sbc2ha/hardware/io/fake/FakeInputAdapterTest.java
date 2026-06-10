package iot.sbc2ha.hardware.io.fake;

import iot.sbc2ha.runtime.DeviceState;
import iot.sbc2ha.hardware.io.InputAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FakeInputAdapter}.
 *
 * <p>Verifies the fake adapter's simulated press/release events
 * properly fire registered listeners, and state transitions work
 * as expected for hardware-independent testing.</p>
 */
class FakeInputAdapterTest {

    @Test
    void initialState_isOff() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        assertEquals(DeviceState.OFF, adapter.read());
    }

    @Test
    void setState_changesStateAndReadReturnsNewValue() {
        FakeInputAdapter adapter = new FakeInputAdapter();

        adapter.setState(DeviceState.ON);
        assertEquals(DeviceState.ON, adapter.read());

        adapter.setState(DeviceState.OFF);
        assertEquals(DeviceState.OFF, adapter.read());
    }

    @Test
    void getState_returnsCurrentState() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        adapter.setState(DeviceState.ON);
        assertEquals(DeviceState.ON, adapter.getState());
    }

    @Test
    void simulatePress_firesListener() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        boolean[] pressed = { false };

        adapter.addInputListener(new InputAdapter.InputListener() {
            @Override public void pressed(long timestamp) { pressed[0] = true; }
            @Override public void released(long timestamp) {}
        });

        adapter.simulatePress();
        assertTrue(pressed[0], "Listener should be notified on simulatePress");
    }

    @Test
    void simulateRelease_firesListener() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        boolean[] released = { false };

        adapter.addInputListener(new InputAdapter.InputListener() {
            @Override public void pressed(long timestamp) {}
            @Override public void released(long timestamp) { released[0] = true; }
        });

        adapter.simulateRelease();
        assertTrue(released[0], "Listener should be notified on simulateRelease");
    }

    @Test
    void simulatePress_withNoListener_doesNothing() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        assertDoesNotThrow(adapter::simulatePress);
    }

    @Test
    void simulateRelease_withNoListener_doesNothing() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        assertDoesNotThrow(adapter::simulateRelease);
    }

    @Test
    void simulatePress_chaining() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        adapter.addInputListener(new InputAdapter.InputListener() {
            @Override public void pressed(long t) {}
            @Override public void released(long t) {}
        });

        assertSame(adapter, adapter.simulatePress());
        assertSame(adapter, adapter.simulatePress().simulateRelease());
    }

    @Test
    void removeInputListener_cancelsCallback() {
        FakeInputAdapter adapter = new FakeInputAdapter();
        InputAdapter.InputListener listener = new InputAdapter.InputListener() {
            @Override public void pressed(long t) {}
            @Override public void released(long t) {}
        };

        adapter.addInputListener(listener);
        adapter.removeInputListener(listener);

        boolean[] fired = { false };
        adapter.addInputListener(new InputAdapter.InputListener() {
            @Override public void pressed(long t) { fired[0] = true; }
            @Override public void released(long t) {}
        });
        adapter.simulatePress();
        assertTrue(fired[0]);
    }
}
