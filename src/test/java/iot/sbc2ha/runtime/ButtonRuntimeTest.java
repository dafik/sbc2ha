package iot.sbc2ha.runtime;

import iot.sbc2ha.device.ButtonDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ButtonRuntime}.
 */
class ButtonRuntimeTest {

    @Test
    void buttonWithoutClickAction_hasNoop() {
        ButtonDevice btn = new ButtonDevice("btn_noop", "No-Op Button", null);
        ButtonRuntime runtime = new ButtonRuntime(btn);

        assertEquals("btn_noop", runtime.id());
        assertEquals(ActionType.NOOP, runtime.action());
        assertNull(runtime.targetId());
    }

    @Test
    void buttonWithClickAction_resolvesToToggle() {
        ButtonDevice btn = new ButtonDevice("btn_click", "Click Button", "target_1");
        ButtonRuntime runtime = new ButtonRuntime(btn, ActionType.OUTPUT_TOGGLE, "target_1");

        assertEquals("btn_click", runtime.id());
        assertEquals(ActionType.OUTPUT_TOGGLE, runtime.action());
        assertEquals("target_1", runtime.targetId());
    }

    @Test
    void buttonConfig_returnsUnderlyingDevice() {
        ButtonDevice btn = new ButtonDevice("btn_cfg", "Config Button", "t");
        ButtonRuntime runtime = new ButtonRuntime(btn, ActionType.NOOP, null);

        assertSame(btn, runtime.config());
    }
}
