package iot.sbc2ha.boot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeBootDisplayTest {

    @Test
    void updateDoesNotThrow() {
        FakeBootDisplay display = new FakeBootDisplay();
        assertDoesNotThrow(() -> display.update(LifecycleState.BOOTING));
    }

    @Test
    void closeDoesNotThrow() {
        FakeBootDisplay display = new FakeBootDisplay();
        assertDoesNotThrow(display::close);
    }

    @Test
    void updateAcceptsAllStates() {
        FakeBootDisplay display = new FakeBootDisplay();
        // Should not throw for any state
        for (LifecycleState state : LifecycleState.values()) {
            assertDoesNotThrow(() -> display.update(state));
        }
    }
}
