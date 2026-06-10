package iot.sbc2ha.runtime;

import iot.sbc2ha.device.SwitchDevice;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

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
        assertEquals(ActionType.NOOP, runtime.action(EventType.CLICK));
        assertNull(runtime.targetId(EventType.CLICK));
    }

    @Test
    void switchWithClickAction_resolvesToToggle() {
        SwitchDevice switch1 = new SwitchDevice("switch_click", "Click Switch", "target_1");
        SwitchRuntime runtime = new SwitchRuntime(switch1, ActionType.OUTPUT_TOGGLE, "target_1");

        assertEquals("switch_click", runtime.id());
        assertEquals(ActionType.OUTPUT_TOGGLE, runtime.action(EventType.CLICK));
        assertEquals("target_1", runtime.targetId(EventType.CLICK));
    }

    @Test
    void switchConfig_returnsUnderlyingDevice() {
        SwitchDevice switch1 = new SwitchDevice("switch_cfg", "Config Switch", "t");
        SwitchRuntime runtime = new SwitchRuntime(switch1, ActionType.NOOP, null);

        assertSame(switch1, runtime.config());
    }

    @Test
    void perEvent_clickIsDefault() {
        SwitchDevice switch1 = new SwitchDevice("switch_legacy", "Legacy Switch", "out_1");
        SwitchRuntime runtime = new SwitchRuntime(switch1, ActionType.OUTPUT_TOGGLE, "out_1");

        // CLICK action is stored as CLICK event
        assertEquals(ActionType.OUTPUT_TOGGLE, runtime.action(EventType.CLICK));
        assertEquals("out_1", runtime.targetId(EventType.CLICK));
    }

    @Test
    void perEvent_unconfiguredEventReturnsNoop() {
        SwitchDevice switch1 = new SwitchDevice("switch_click_only", "Click Switch", "out_1");
        SwitchRuntime runtime = new SwitchRuntime(switch1, ActionType.OUTPUT_TOGGLE, "out_1");

        // Only CLICK is configured; DOUBLE/LONG/RELEASE should be NOOP
        assertEquals(ActionType.NOOP, runtime.action(EventType.DOUBLE));
        assertNull(runtime.targetId(EventType.DOUBLE));
        assertEquals(ActionType.NOOP, runtime.action(EventType.LONG));
        assertNull(runtime.targetId(EventType.LONG));
        assertEquals(ActionType.NOOP, runtime.action(EventType.RELEASE));
        assertNull(runtime.targetId(EventType.RELEASE));
    }

    @Test
    void perEvent_multiEventWiring() {
        SwitchDevice switch1 = new SwitchDevice("switch_multi", "Multi Switch", null);

        var eventActions = new EnumMap<EventType, ActionType>(EventType.class);
        eventActions.put(EventType.CLICK, ActionType.OUTPUT_TOGGLE);
        eventActions.put(EventType.DOUBLE, ActionType.OUTPUT_ON);
        eventActions.put(EventType.LONG, ActionType.OUTPUT_OFF);

        var eventTargets = new EnumMap<EventType, String>(EventType.class);
        eventTargets.put(EventType.CLICK, "out_1");
        eventTargets.put(EventType.DOUBLE, "out_2");
        eventTargets.put(EventType.LONG, "out_3");

        SwitchRuntime runtime = new SwitchRuntime(switch1, eventActions, eventTargets);

        // Per-event lookups
        assertEquals(ActionType.OUTPUT_TOGGLE, runtime.action(EventType.CLICK));
        assertEquals("out_1", runtime.targetId(EventType.CLICK));

        assertEquals(ActionType.OUTPUT_ON, runtime.action(EventType.DOUBLE));
        assertEquals("out_2", runtime.targetId(EventType.DOUBLE));

        assertEquals(ActionType.OUTPUT_OFF, runtime.action(EventType.LONG));
        assertEquals("out_3", runtime.targetId(EventType.LONG));

        assertEquals(ActionType.NOOP, runtime.action(EventType.RELEASE));
        assertNull(runtime.targetId(EventType.RELEASE));
    }
}
