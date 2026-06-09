package iot.sbc2ha.runtime;

import iot.sbc2ha.device.SwitchDevice;
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
        assertEquals(ActionType.OUTPUT_TOGGLE, switch1.action());
        assertEquals("out_1", switch1.targetId());

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
        assertEquals(ActionType.OUTPUT_TOGGLE, switch1.action());

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
        assertEquals(ActionType.NOOP, switch1.action());
        assertNull(switch1.targetId());
    }

    @Test
    void dispatchClick_togglesOutput() {
        DeviceRegistry registry = new DeviceRegistry();
        registry.add(new OutputDevice("out_1", "Relay 1"));
        registry.add(new SwitchDevice("switch_1", "Switch 1", "out_1"));
        registry.validate();

        ActionEngine engine = new ActionEngine(registry);

        engine.dispatchClick(engine.getSwitch("switch_1"));
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchClick(engine.getSwitch("switch_1"));
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
        engine.dispatchClick(engine.getSwitch("switch_noop"));
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
        engine.dispatchClick(engine.getSwitch("switch_1"));
        assertEquals(DeviceState.ON, ((OutputRuntime) engine.getTarget("out_1")).state());

        engine.dispatchClick(engine.getSwitch("switch_2"));
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

        engine.dispatchClick(switch1);
        assertEquals(DeviceState.ON, output.state());

        engine.dispatchClick(switch1);
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

        engine.dispatchClick(switch1);
        assertEquals(DeviceState.OFF, output.state());

        // Ensure ON state can be set first
        output.setState(DeviceState.ON);
        engine.dispatchClick(switch1);
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
}
