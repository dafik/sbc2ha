package iot.sbc2ha.runtime;

import iot.sbc2ha.device.SwitchDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SwitchRuntime}.
 */
class SwitchRuntimeTest {

    @Test
    void switchWithoutClickAction_hasNoop() {
        SwitchDevice switch1 = new SwitchDevice("switch_noop", "No-Op Switch", null);
        SwitchRuntime runtime = new SwitchRuntime(switch1);

        assertEquals("switch_noop", runtime.id());
        assertEquals(ActionType.NOOP, runtime.action());
        assertNull(runtime.targetId());
    }

    @Test
    void switchWithClickAction_resolvesToToggle() {
        SwitchDevice switch1 = new SwitchDevice("switch_click", "Click Switch", "target_1");
        SwitchRuntime runtime = new SwitchRuntime(switch1, ActionType.OUTPUT_TOGGLE, "target_1");

        assertEquals("switch_click", runtime.id());
        assertEquals(ActionType.OUTPUT_TOGGLE, runtime.action());
        assertEquals("target_1", runtime.targetId());
    }

    @Test
    void switchConfig_returnsUnderlyingDevice() {
        SwitchDevice switch1 = new SwitchDevice("switch_cfg", "Config Switch", "t");
        SwitchRuntime runtime = new SwitchRuntime(switch1, ActionType.NOOP, null);

        assertSame(switch1, runtime.config());
    }
}
