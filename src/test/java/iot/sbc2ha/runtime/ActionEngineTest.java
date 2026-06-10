package iot.sbc2ha.runtime;

import iot.sbc2ha.device.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ActionEngine}.
 */
class ActionEngineTest {

    @Test
    void engineWiresSwitchToOutput() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        assertEquals(1, engine.switchs().size());
        assertEquals(1, engine.targets().size());

        SwitchRuntime switch1 = engine.getSwitch("switch_1");
        assertNotNull(switch1);
        assertEquals(ActionType.OUTPUT_TOGGLE, switch1.action(EventType.CLICK));
        assertEquals("out_1", switch1.targetId(EventType.CLICK));

        DeviceRuntime target = engine.getTarget("out_1");
        assertNotNull(target);
        assertInstanceOf(OutputRuntime.class, target);
        assertEquals(DeviceState.OFF, ((OutputRuntime) target).state());
    }

    @Test
    void engineWiresSwitchToLight() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new LightDevice("light_1", "Kitchen Light"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "light_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        SwitchRuntime switch1 = engine.getSwitch("switch_1");
        assertNotNull(switch1);
        assertEquals(ActionType.OUTPUT_TOGGLE, switch1.action(EventType.CLICK));

        DeviceRuntime target = engine.getTarget("light_1");
        assertNotNull(target);
        assertInstanceOf(LightRuntime.class, target);
    }

    @Test
    void engineWiresSwitchWithoutClickAction() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_noop", "No-Op Switch", null));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        SwitchRuntime switch1 = engine.getSwitch("switch_noop");
        assertNotNull(switch1);
        assertEquals(ActionType.NOOP, switch1.action(EventType.CLICK));
        assertNull(switch1.targetId(EventType.CLICK));
    }

    @Test
    void dispatchClick_togglesOutput() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        engine.dispatchEvent(engine.getSwitch("switch_1"), EventType.CLICK);
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchEvent(engine.getSwitch("switch_1"), EventType.CLICK);
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchClick_noop_doesNothing() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_noop", "No-Op Switch", null));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");
        engine.dispatchEvent(engine.getSwitch("switch_noop"), EventType.CLICK);
        assertEquals(DeviceState.OFF, output.state());
    }

    @Test
    void dispatchClick_unknownTarget_logsError() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        // Create a switch with an unknown target (skip validation for this test)
        SwitchRuntime switch1 = new SwitchRuntime(
                new SwitchDevice("switch_bad", "Bad Switch", "nonexistent"),
                ActionType.OUTPUT_TOGGLE, "nonexistent");

        ActionEngine engine = new ActionEngine(registry);
        // Manually inject the bad switch (since registry validation would reject it)
        engine.switchs().put("switch_bad", switch1);

        // Should not throw, just log error
        assertDoesNotThrow(() -> engine.dispatchClick(switch1));
    }

    @Test
    void multipleSwitchsSameTarget() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.add(new SwitchDevice("switch_2", "Switch 2", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        assertEquals(2, engine.switchs().size());
        assertEquals(1, engine.targets().size());

        // Both switchs should toggle the same output
        engine.dispatchEvent(engine.getSwitch("switch_1"), EventType.CLICK);
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchEvent(engine.getSwitch("switch_2"), EventType.CLICK);
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchOutputOn_setsToOn() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");

        // Manually create an OUTPUT_ON switch
        SwitchRuntime switch1 = new SwitchRuntime(
                new SwitchDevice("switch_on", "On Switch", "out_1"),
                ActionType.OUTPUT_ON, "out_1");
        engine.switchs().put("switch_on", switch1);

        engine.dispatchEvent(switch1, EventType.CLICK);
        assertEquals(DeviceState.ON, output.state());

        engine.dispatchEvent(switch1, EventType.CLICK);
        assertEquals(DeviceState.ON, output.state());
    }

    @Test
    void dispatchOutputOff_setsToOff() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");

        SwitchRuntime switch1 = new SwitchRuntime(
                new SwitchDevice("switch_off", "Off Switch", "out_1"),
                ActionType.OUTPUT_OFF, "out_1");
        engine.switchs().put("switch_off", switch1);

        engine.dispatchEvent(switch1, EventType.CLICK);
        assertEquals(DeviceState.OFF, output.state());

        // Ensure ON state can be set first
        output.setState(DeviceState.ON);
        engine.dispatchEvent(switch1, EventType.CLICK);
        assertEquals(DeviceState.OFF, output.state());
    }

    @Test
    void withStateService_restoresInitialStates(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("state.json");

        // Pre-populate state file with ON state for out_1
        StateService stateService = new StateService(stateFile);
        stateService.setState("out_1", DeviceState.ON);
        stateService.persist();

        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry, stateService);

        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");
        assertEquals(DeviceState.ON, output.state());
    }

    @Test
    void withStateService_noRestoreForUnknownDevices(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("state.json");

        StateService stateService = new StateService(stateFile);
        stateService.setState("out_1", DeviceState.ON);
        stateService.setState("unknown_dev", DeviceState.ON);
        stateService.persist();

        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry, stateService);

        // out_1 should be restored
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");
        assertEquals(DeviceState.ON, output.state());

        // unknown_dev should not appear
        assertNull(engine.getTarget("unknown_dev"));
    }

    @Test
    void withStateService_dispatchPersistStates(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("state.json");

        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        StateService stateService = new StateService(stateFile);
        ActionEngine engine = new ActionEngine(registry, stateService);

        // Dispatch a click
        engine.dispatchClick(engine.getSwitch("switch_1"));

        // Verify state was persisted to disk
        StateService fresh = new StateService(stateFile);
        Map<String, DeviceState> restored = fresh.load();
        assertEquals(1, restored.size());
        assertEquals(DeviceState.ON, restored.get("out_1"));
    }

    @Test
    void withStateService_null_service_noPersist() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry, null);

        engine.dispatchClick(engine.getSwitch("switch_1"));
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());
        assertNull(engine.stateService());
    }

    @Test
    void withStateService_restoresLight(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("state.json");

        StateService stateService = new StateService(stateFile);
        stateService.setState("light_1", DeviceState.ON);
        stateService.persist();

        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new LightDevice("light_1", "Kitchen Light"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry, stateService);

        LightRuntime light = (LightRuntime) engine.getTarget("light_1");
        assertEquals(DeviceState.ON, light.state());
    }

    @Test
    void engineIndexesInputDevices() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false));
        registry.add(new InputDevice("motion_1", "Motion 1", InputDevice.SensorType.MOTION, false));
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        // Input devices are indexed in targets
        assertNotNull(engine.getTarget("door_1"));
        assertNotNull(engine.getTarget("motion_1"));
        assertNotNull(engine.getTarget("out_1"));
        assertInstanceOf(InputRuntime.class, engine.getTarget("door_1"));
        assertInstanceOf(InputRuntime.class, engine.getTarget("motion_1"));
        assertInstanceOf(OutputRuntime.class, engine.getTarget("out_1"));
    }

    @Test
    void getInput_returnsInputRuntime() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        InputRuntime input = engine.getInput("door_1");
        assertNotNull(input);
        assertEquals(DeviceState.OFF, input.state());
    }

    @Test
    void getInput_returnsNullForNonInput() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        assertNull(engine.getInput("out_1"));
    }

    @Test
    void dispatchInput_updatesState() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        InputRuntime input = engine.getInput("door_1");
        assertNotNull(input);
        assertEquals(DeviceState.OFF, input.state());

        engine.dispatchInput("door_1", DeviceState.ON);
        assertEquals(DeviceState.ON, input.state());
        assertEquals(DeviceState.ON, input.rawState());
    }

    @Test
    void dispatchInput_invertedSensor() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new InputDevice("contact_1", "Contact 1", InputDevice.SensorType.CONTACT, true));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        InputRuntime input = engine.getInput("contact_1");
        assertNotNull(input);
        // Inverted sensor: raw OFF → logical ON (default raw state)
        assertEquals(DeviceState.ON, input.state());

        // Raw ON on inverted → logical OFF
        engine.dispatchInput("contact_1", DeviceState.ON);
        assertEquals(DeviceState.OFF, input.state());
        assertEquals(DeviceState.ON, input.rawState());

        // Raw OFF on inverted → logical ON
        engine.dispatchInput("contact_1", DeviceState.OFF);
        assertEquals(DeviceState.ON, input.state());
        assertEquals(DeviceState.OFF, input.rawState());
    }

    @Test
    void dispatchInput_unknownDevice_doesNothing() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        assertFalse(engine.dispatchInput("nonexistent", DeviceState.ON));
    }

    @Test
    void engineWithStateService_doesNotRestoreInputDevices(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("state.json");

        StateService stateService = new StateService(stateFile);
        stateService.setState("door_1", DeviceState.ON);
        stateService.persist();

        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry, stateService);

        // Input devices are NOT restored from state — they start OFF
        InputRuntime input = engine.getInput("door_1");
        assertNotNull(input);
        assertEquals(DeviceState.OFF, input.state());
    }

    // ---- SBC-012: multi-event dispatch ----

    @Test
    void dispatchEvent_click_togglesOutput_viaSwitch() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        SwitchRuntime sw = engine.getSwitch("switch_1");

        engine.dispatchEvent(sw, EventType.CLICK);
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchEvent(sw, EventType.CLICK);
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchEvent_double_withNoConfig_doesNothing() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        SwitchRuntime sw = engine.getSwitch("switch_1");

        // DOUBLE is not configured → NOOP → no state change
        engine.dispatchEvent(sw, EventType.DOUBLE);
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchEvent_longWithNoop_doesNothing() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        SwitchRuntime sw = engine.getSwitch("switch_1");

        // LONG is not configured → NOOP with null target → skip
        engine.dispatchEvent(sw, EventType.LONG);
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchEvent_releaseWithNoop_doesNothing() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        SwitchRuntime sw = engine.getSwitch("switch_1");

        // RELEASE is not configured → NOOP with null target → skip
        engine.dispatchEvent(sw, EventType.RELEASE);
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchEvent_outputOn_action() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");

        // Manually create a switch with OUTPUT_ON for CLICK
        SwitchRuntime sw = new SwitchRuntime(
                new SwitchDevice("switch_on", "On Switch", "out_1"),
                new java.util.EnumMap<>(java.util.Map.of(EventType.CLICK, ActionType.OUTPUT_ON)),
                new java.util.EnumMap<>(java.util.Map.of(EventType.CLICK, "out_1")));
        engine.switchs().put("switch_on", sw);

        engine.dispatchEvent(sw, EventType.CLICK);
        assertEquals(DeviceState.ON, output.state());

        // Double-click has NOOP → no change
        engine.dispatchEvent(sw, EventType.DOUBLE);
        assertEquals(DeviceState.ON, output.state());
    }

    @Test
    void dispatchEvent_outputOff_action() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");
        output.setState(DeviceState.ON);

        SwitchRuntime sw = new SwitchRuntime(
                new SwitchDevice("switch_off", "Off Switch", "out_1"),
                new java.util.EnumMap<>(java.util.Map.of(EventType.CLICK, ActionType.OUTPUT_OFF)),
                new java.util.EnumMap<>(java.util.Map.of(EventType.CLICK, "out_1")));
        engine.switchs().put("switch_off", sw);

        engine.dispatchEvent(sw, EventType.CLICK);
        assertEquals(DeviceState.OFF, output.state());
    }

    @Test
    void dispatchEvent_dispatchClick_delegatesToClick() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");

        // Legacy dispatchClick should delegate to dispatchEvent(CLICK)
        SwitchRuntime sw = engine.getSwitch("switch_1");
        Runnable click1 = () -> engine.dispatchClick(sw);
        click1.run();

        assertEquals(DeviceState.ON, output.state());

        Runnable click2 = () -> engine.dispatchClick(sw);
        click2.run();

        assertEquals(DeviceState.OFF, output.state());
    }

    @Test
    void dispatchEvent_toLight_toggles() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new LightDevice("light_1", "Kitchen Light"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "light_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        SwitchRuntime sw = engine.getSwitch("switch_1");

        engine.dispatchEvent(sw, EventType.CLICK);
        assertEquals(DeviceState.ON, ((LightRuntime) engine.getTarget("light_1")).state());

        engine.dispatchEvent(sw, EventType.CLICK);
        assertEquals(DeviceState.OFF, ((LightRuntime) engine.getTarget("light_1")).state());
    }

    @Test
    void dispatchEvent_unknownTarget_logsError() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        SwitchRuntime sw = new SwitchRuntime(
                new SwitchDevice("switch_bad", "Bad Switch", "nonexistent"),
                ActionType.OUTPUT_TOGGLE, "nonexistent");

        // Should not throw, just log error
        assertDoesNotThrow(() -> engine.dispatchEvent(sw, EventType.CLICK));
    }

    @Test
    void dispatchEvent_withStateService_persistsState(@TempDir Path tempDir) {
        Path stateFile = tempDir.resolve("state.json");

        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        StateService stateService = new StateService(stateFile);
        ActionEngine engine = new ActionEngine(registry, stateService);

        engine.dispatchEvent(engine.getSwitch("switch_1"), EventType.CLICK);

        StateService fresh = new StateService(stateFile);
        Map<String, DeviceState> restored = fresh.load();
        assertEquals(1, restored.size());
        assertEquals(DeviceState.ON, restored.get("out_1"));
    }
}
