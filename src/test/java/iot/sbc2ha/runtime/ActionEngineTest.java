package iot.sbc2ha.runtime;

import iot.sbc2ha.device.ButtonDevice;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.LightDevice;
import iot.sbc2ha.device.OutputDevice;
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
    void engineWiresButtonToOutput() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new ButtonDevice("btn_1", "Button 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        assertEquals(1, engine.buttons().size());
        assertEquals(1, engine.targets().size());

        ButtonRuntime btn = engine.getButton("btn_1");
        assertNotNull(btn);
        assertEquals(ActionType.OUTPUT_TOGGLE, btn.action());
        assertEquals("out_1", btn.targetId());

        DeviceRuntime target = engine.getTarget("out_1");
        assertNotNull(target);
        assertInstanceOf(OutputRuntime.class, target);
        assertEquals(DeviceState.OFF, ((OutputRuntime) target).state());
    }

    @Test
    void engineWiresButtonToLight() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new LightDevice("light_1", "Kitchen Light"));
        registry.add(new ButtonDevice("btn_1", "Button 1", "light_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        ButtonRuntime btn = engine.getButton("btn_1");
        assertNotNull(btn);
        assertEquals(ActionType.OUTPUT_TOGGLE, btn.action());

        DeviceRuntime target = engine.getTarget("light_1");
        assertNotNull(target);
        assertInstanceOf(LightRuntime.class, target);
    }

    @Test
    void engineWiresButtonWithoutClickAction() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new ButtonDevice("btn_noop", "No-Op Button", null));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        ButtonRuntime btn = engine.getButton("btn_noop");
        assertNotNull(btn);
        assertEquals(ActionType.NOOP, btn.action());
        assertNull(btn.targetId());
    }

    @Test
    void dispatchClick_togglesOutput() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new ButtonDevice("btn_1", "Button 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        engine.dispatchClick(engine.getButton("btn_1"));
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchClick(engine.getButton("btn_1"));
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchClick_noop_doesNothing() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new ButtonDevice("btn_noop", "No-Op Button", null));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");
        engine.dispatchClick(engine.getButton("btn_noop"));
        assertEquals(DeviceState.OFF, output.state());
    }

    @Test
    void dispatchClick_unknownTarget_logsError() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        // Create a button with an unknown target (skip validation for this test)
        ButtonRuntime btn = new ButtonRuntime(
                new ButtonDevice("btn_bad", "Bad Button", "nonexistent"),
                ActionType.OUTPUT_TOGGLE, "nonexistent");

        ActionEngine engine = new ActionEngine(registry);
        // Manually inject the bad button (since registry validation would reject it)
        engine.buttons().put("btn_bad", btn);

        // Should not throw, just log error
        assertDoesNotThrow(() -> engine.dispatchClick(btn));
    }

    @Test
    void multipleButtonsSameTarget() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new ButtonDevice("btn_1", "Button 1", "out_1"));
        registry.add(new ButtonDevice("btn_2", "Button 2", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        assertEquals(2, engine.buttons().size());
        assertEquals(1, engine.targets().size());

        // Both buttons should toggle the same output
        engine.dispatchClick(engine.getButton("btn_1"));
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchClick(engine.getButton("btn_2"));
        assertEquals(DeviceState.OFF, ((OutputRuntime) engine.getTarget("out_1")).state());
    }

    @Test
    void dispatchOutputOn_setsToOn() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");

        // Manually create an OUTPUT_ON button
        ButtonRuntime btn = new ButtonRuntime(
                new ButtonDevice("btn_on", "On Button", "out_1"),
                ActionType.OUTPUT_ON, "out_1");
        engine.buttons().put("btn_on", btn);

        engine.dispatchClick(btn);
        assertEquals(DeviceState.ON, output.state());

        engine.dispatchClick(btn);
        assertEquals(DeviceState.ON, output.state());
    }

    @Test
    void dispatchOutputOff_setsToOff() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);
        OutputRuntime output = (OutputRuntime) engine.getTarget("out_1");

        ButtonRuntime btn = new ButtonRuntime(
                new ButtonDevice("btn_off", "Off Button", "out_1"),
                ActionType.OUTPUT_OFF, "out_1");
        engine.buttons().put("btn_off", btn);

        engine.dispatchClick(btn);
        assertEquals(DeviceState.OFF, output.state());

        // Ensure ON state can be set first
        output.setState(DeviceState.ON);
        engine.dispatchClick(btn);
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
        registry.add(new ButtonDevice("btn_1", "Button 1", "out_1"));
        registry.validate();

        StateService stateService = new StateService(stateFile);
        ActionEngine engine = new ActionEngine(registry, stateService);

        // Dispatch a click
        engine.dispatchClick(engine.getButton("btn_1"));

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
        registry.add(new ButtonDevice("btn_1", "Button 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry, null);

        engine.dispatchClick(engine.getButton("btn_1"));
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
}
