package iot.sbc2ha;

import iot.sbc2ha.boot.BootDisplay;
import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.ConfigLoader;
import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.runtime.ActionEngine;
import iot.sbc2ha.runtime.ActionType;
import iot.sbc2ha.runtime.ButtonRuntime;
import iot.sbc2ha.runtime.DeviceRuntime;
import iot.sbc2ha.runtime.DeviceState;
import iot.sbc2ha.runtime.LightRuntime;
import iot.sbc2ha.runtime.OutputRuntime;
import iot.sbc2ha.runtime.StateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SBC-010: Fake offline-ready integration test.
 *
 * <p>End-to-end fake pipeline: load config → restore state → build fake runtime →
 * reach OFFLINE_READY → toggle output via button click → verify state persists.</p>
 */
class FakeOfflineReadyIntegrationTest {

    @TempDir
    Path tempDir;

    private static final String BONE1_YAML = """
            sbc2ha:
              id: bone1
              name: bone1
              schema: 1
            runtime:
              offline_first: true
            hardware:
              board: { id: main, type: beaglebone-black }
              buses:
                i2c2: { type: i2c, path: /dev/i2c-2 }
              profiles:
                input_board: { type: boneio.input-v0.4, board: main }
                output_board:
                  type: boneio.output-24x16a-v0.4
                  bus: i2c2
                  addresses: { mcp1: 0x20, mcp2: 0x21 }
            locations:
              floor1: { name: Piętro 1 }
              floor1.stairs: { name: Klatka, parent: floor1 }
            devices:
              - id: klatka_button
                name: Klatka 1
                type: button
                input: input_board.input1
                location: floor1.stairs
                clicks: { click: true, double: true, long: false, release: false }
                actions:
                  click:
                    - { type: output.toggle, target: klatka_light }
                expose:
                  ha: { enabled: true, events: [click, double] }
              - id: klatka_light
                name: Klatka
                type: light
                output: output_board.output1
                location: floor1.stairs
                restore_state: true
                expose:
                  ha: { enabled: true }
            mqtt: { enabled: false }
            home_assistant: { enabled: false }
            """;

    /**
     * Full pipeline with new actions format:
     * config load → lifecycle → state restore → ActionEngine → OFFLINE_READY → toggle.
     */
    @Test
    void fullPipeline_reachesOfflineReadyAndTogglesOutput() throws Exception {
        // --- Setup: write YAML and load config ---
        Path yamlFile = tempDir.resolve("bone1.yaml");
        Files.writeString(yamlFile, BONE1_YAML);

        Sbc2haConfig config = ConfigLoader.load(yamlFile);
        assertEquals("bone1", config.nodeId());
        assertEquals("1", config.schema());

        // --- Lifecycle: BOOTING → CONFIG_LOADED ---
        FakeBootDisplay display = new FakeBootDisplay();
        Lifecycle lifecycle = new Lifecycle(display);
        lifecycle.transition(LifecycleState.BOOTING);
        assertEquals(LifecycleState.BOOTING, lifecycle.state());

        lifecycle.transition(LifecycleState.CONFIG_LOADED);
        assertEquals(LifecycleState.CONFIG_LOADED, lifecycle.state());

        // --- Device registry ---
        DeviceRegistry registry = config.registry();
        assertNotNull(registry);
        assertEquals(2, registry.size());

        // --- StateService: empty initially ---
        Path stateFile = tempDir.resolve("state.json");
        StateService stateService = new StateService(stateFile);
        Map<String, DeviceState> restored = stateService.load();
        assertTrue(restored.isEmpty());

        // --- ActionEngine: wire buttons and targets ---
        ActionEngine engine = new ActionEngine(registry, stateService);

        // Button wired with OUTPUT_TOGGLE (new actions format, not legacy clickAction)
        ButtonRuntime btnRuntime = engine.getButton("klatka_button");
        assertNotNull(btnRuntime);
        assertEquals(ActionType.OUTPUT_TOGGLE, btnRuntime.action());
        assertEquals("klatka_light", btnRuntime.targetId());

        // Light is a togglable target
        DeviceRuntime lightTarget = engine.getTarget("klatka_light");
        assertNotNull(lightTarget);
        assertInstanceOf(LightRuntime.class, lightTarget);
        LightRuntime lightRuntime = (LightRuntime) lightTarget;
        assertEquals(DeviceState.OFF, lightRuntime.state());

        // --- Lifecycle: STATE_RESTORED → OFFLINE_READY ---
        lifecycle.transition(LifecycleState.STATE_RESTORED);
        assertEquals(LifecycleState.STATE_RESTORED, lifecycle.state());

        lifecycle.transition(LifecycleState.OFFLINE_READY);
        assertEquals(LifecycleState.OFFLINE_READY, lifecycle.state());

        // --- Toggle: simulate button click ---
        engine.dispatchClick(btnRuntime);
        assertEquals(DeviceState.ON, lightRuntime.state());

        // Verify state persisted
        Map<String, DeviceState> persisted = stateService.getAllStates();
        assertEquals(1, persisted.size());
        assertEquals(DeviceState.ON, persisted.get("klatka_light"));

        // Toggle again
        engine.dispatchClick(btnRuntime);
        assertEquals(DeviceState.OFF, lightRuntime.state());
        assertEquals(DeviceState.OFF, stateService.getState("klatka_light"));

        // --- Cleanup ---
        lifecycle.shutdown();
    }

    /**
     * Verify that a fresh start restores persisted state from a previous toggle.
     */
    @Test
    void restoreFromPersistedState_appliesPreviousState() throws Exception {
        Path yamlFile = tempDir.resolve("restore-test.yaml");
        Files.writeString(yamlFile, BONE1_YAML);

        Sbc2haConfig config = ConfigLoader.load(yamlFile);
        DeviceRegistry registry = config.registry();

        Path stateFile = tempDir.resolve("restore-state.json");

        // Simulate: write a persisted state file with light ON
        var wrapper = new StateService.VersionedState("1",
                Map.of("klatka_light", DeviceState.ON));
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = mapper.writeValueAsString(wrapper);
        Files.writeString(stateFile, json);

        // Build ActionEngine with StateService
        StateService stateService = new StateService(stateFile);
        ActionEngine engine = new ActionEngine(registry, stateService);

        // Light should be restored to ON
        DeviceRuntime light = engine.getTarget("klatka_light");
        assertNotNull(light);
        assertInstanceOf(LightRuntime.class, light);
        LightRuntime lightRuntime = (LightRuntime) light;
        assertEquals(DeviceState.ON, lightRuntime.state());

        lifecycleShutdown();
    }

    /**
     * Verify legacy format (click_action) still works.
     */
    @Test
    void legacyClickAction_stillTogglesOutput() throws Exception {
        String legacyYaml = """
                node_id: legacy-node
                schema: "1"
                devices:
                  - id: btn_1
                    type: button
                    display_name: Button 1
                    click_action: light_1
                  - id: light_1
                    type: output
                    display_name: Light 1
                """;
        Path yamlFile = tempDir.resolve("legacy.yaml");
        Files.writeString(yamlFile, legacyYaml);

        Sbc2haConfig config = ConfigLoader.load(yamlFile);
        DeviceRegistry registry = config.registry();
        StateService stateService = new StateService(tempDir.resolve("legacy-state.json"));
        ActionEngine engine = new ActionEngine(registry, stateService);

        ButtonRuntime btn = engine.getButton("btn_1");
        assertNotNull(btn);
        assertEquals(ActionType.OUTPUT_TOGGLE, btn.action());
        assertEquals("light_1", btn.targetId());

        DeviceRuntime target = engine.getTarget("light_1");
        assertNotNull(target);
        assertInstanceOf(OutputRuntime.class, target);
        OutputRuntime out = (OutputRuntime) target;
        assertEquals(DeviceState.OFF, out.state());

        engine.dispatchClick(btn);
        assertEquals(DeviceState.ON, out.state());

        lifecycleShutdown();
    }

    /**
     * Verify config load → OFFLINE_READY lifecycle with empty device list.
     */
    @Test
    void emptyDevices_reachesOfflineReady() throws Exception {
        String emptyYaml = """
                sbc2ha:
                  id: empty-node
                  schema: 1
                devices: []
                mqtt: { enabled: false }
                home_assistant: { enabled: false }
                """;
        Path yamlFile = tempDir.resolve("empty.yaml");
        Files.writeString(yamlFile, emptyYaml);

        Sbc2haConfig config = ConfigLoader.load(yamlFile);
        assertEquals("empty-node", config.nodeId());
        assertEquals(0, config.devices().size());

        FakeBootDisplay display = new FakeBootDisplay();
        Lifecycle lifecycle = new Lifecycle(display);

        lifecycle.transition(LifecycleState.BOOTING);
        lifecycle.transition(LifecycleState.CONFIG_LOADED);
        lifecycle.transition(LifecycleState.STATE_RESTORED);
        lifecycle.transition(LifecycleState.OFFLINE_READY);

        assertEquals(LifecycleState.OFFLINE_READY, lifecycle.state());

        lifecycle.shutdown();
    }

    // --- Helpers ---

    private static void lifecycleShutdown() {
        // nothing to shut down for now
    }

    /**
     * Minimal fake boot display for testing.
     */
    private static class FakeBootDisplay implements BootDisplay {
        @Override
        public void update(LifecycleState state) {
            // no-op — state not asserted in tests
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
