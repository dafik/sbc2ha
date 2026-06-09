package iot.sbc2ha.config;

import iot.sbc2ha.device.*;
import iot.sbc2ha.runtime.ActionEngine;
import iot.sbc2ha.runtime.ActionType;
import iot.sbc2ha.runtime.ButtonRuntime;
import iot.sbc2ha.runtime.LightRuntime;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.deleteIfExists;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for SBC-009: bone1 minimal profile-based example.
 *
 * <p>Validates that {@code bone1-minimal.yaml} parses, validates,
 * and feeds into the fake runtime pipeline.</p>
 */
class Sbc009Bone1MinimalTest {

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
     * Full pipeline: write YAML → load → validate → ActionEngine.
     */
    @Test
    void fullPipeline_parsesAndFeedsFakeRuntime() {
        // Write YAML to temp file
        Path tempFile = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("bone1", ".yaml");
            java.nio.file.Files.writeString(tempFile, BONE1_YAML);

            // Load and validate
            Sbc2haConfig config = ConfigLoader.load(tempFile);

            // Verify node identity (from sbc2ha.id, not root node_id)
            assertEquals("bone1", config.nodeId());
            assertEquals("1", config.schema());

            // Verify runtime config
            assertNotNull(config.runtime());
            assertTrue(config.runtime().isOfflineFirst());

            // Verify MQTT/HA disabled
            assertNotNull(config.mqtt());
            assertFalse(config.mqtt().isEnabled());
            assertNotNull(config.homeAssistant());
            assertFalse(config.homeAssistant().isEnabled());

            // Verify locations
            assertEquals(2, config.locations().size());
            assertEquals("Piętro 1", config.locations().get("floor1").name());
            assertEquals("Klatka", config.locations().get("floor1.stairs").name());
            assertEquals("floor1", config.locations().get("floor1.stairs").parent());

            // Verify hardware config
            assertNotNull(config.hardware());
            assertNotNull(config.hardware().board());
            assertEquals("beaglebone-black", config.hardware().board().type());
            assertEquals(1, config.hardware().buses().size());
            assertEquals("/dev/i2c-2", config.hardware().buses().get("i2c2").path());
            assertEquals(2, config.hardware().profiles().size());

            // Verify devices
            assertEquals(2, config.devices().size());

            // Button device
            ButtonDevice button = (ButtonDevice) config.devices().getFirst();
            assertEquals("klatka_button", button.id());
            assertEquals("Klatka 1", button.name());
            assertEquals("input_board.input1", button.input());
            assertEquals("floor1.stairs", button.location());

            // Verify clicks config
            assertNotNull(button.clicks());
            assertTrue(button.clicks().click());
            assertTrue(button.clicks().dbl());
            assertFalse(button.clicks().longPress());
            assertFalse(button.clicks().release());

            // Verify actions
            assertNotNull(button.actions());
            assertTrue(button.actions().containsKey("click"));
            List<ActionMapping> clickActions = button.actions().get("click");
            assertEquals(1, clickActions.size());
            ActionMapping clickAction = clickActions.getFirst();
            assertEquals(ActionMapping.ActionType.OUTPUT_TOGGLE, clickAction.type());
            assertEquals("klatka_light", clickAction.target());

            // Verify HA expose
            assertNotNull(button.expose());
            assertNotNull(button.expose().ha());
            assertTrue(button.expose().ha().enabled());
            assertEquals(List.of("click", "double"), button.expose().ha().events());

            // Light device
            LightDevice light = (LightDevice) config.devices().get(1);
            assertEquals("klatka_light", light.id());
            assertEquals("Klatka", light.name());
            assertEquals("output_board.output1", light.output());
            assertEquals("floor1.stairs", light.location());
            assertTrue(light.restoreState());

            // Verify HA expose for light
            assertNotNull(light.expose());
            assertNotNull(light.expose().ha());
            assertTrue(light.expose().ha().enabled());
            assertTrue(light.expose().ha().events().isEmpty());

            // Build device registry and validate
            config.validate();
            DeviceRegistry registry = config.registry();
            assertNotNull(registry);

            // Feed to fake runtime (ActionEngine)
            ActionEngine engine = new ActionEngine(registry);
            assertNotNull(engine);

            // Button is wired (legacy clickAction is null → NOOP)
            ButtonRuntime btnRuntime = engine.getButton("klatka_button");
            assertNotNull(btnRuntime);
            assertEquals(ActionType.NOOP, btnRuntime.action());

            // Light is a togglable target
            LightRuntime lightRuntime = (LightRuntime) engine.getTarget("klatka_light");
            assertNotNull(lightRuntime);
        } catch (Exception e) {
            fail("Pipeline failed: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Verify name alias: when display_name is absent, name is used.
     */
    @Test
    void nameAlias_usedWhenDisplayNameAbsent() {
        String yaml = """
                node_id: test-node
                schema: "1"
                devices:
                  - type: light
                    id: light_1
                    name: Display Name
                """;
        Path tempFile = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("alias", ".yaml");
            java.nio.file.Files.writeString(tempFile, yaml);
            Sbc2haConfig config = ConfigLoader.load(tempFile);
            LightDevice light = (LightDevice) config.devices().getFirst();
            assertEquals("Display Name", light.name());
            assertEquals("Display Name", light.displayName());
        } catch (Exception e) {
            fail("Alias test failed: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Verify sbc2ha wrapper provides nodeId and schema.
     */
    @Test
    void sbc2haWrapper_providesIdentity() {
        String yaml = """
                sbc2ha:
                  id: wrapped-node
                  schema: 1
                devices: []
                """;
        Path tempFile = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("wrapped", ".yaml");
            java.nio.file.Files.writeString(tempFile, yaml);
            Sbc2haConfig config = ConfigLoader.load(tempFile);
            assertEquals("wrapped-node", config.nodeId());
            assertEquals("1", config.schema());
        } catch (Exception e) {
            fail("Wrapper test failed: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Legacy format still works: root-level node_id + schema.
     */
    @Test
    void legacyFormat_stillWorks() {
        String yaml = """
                node_id: legacy-node
                schema: "1"
                devices:
                  - type: light
                    id: light_1
                    display_name: Legacy Light
                  - type: button
                    id: btn_1
                    display_name: Legacy Button
                    click_action: light_1
                """;
        Path tempFile = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("legacy", ".yaml");
            java.nio.file.Files.writeString(tempFile, yaml);
            Sbc2haConfig config = ConfigLoader.load(tempFile);
            assertEquals("legacy-node", config.nodeId());
            assertEquals("1", config.schema());
            assertEquals(2, config.devices().size());

            // Legacy format uses display_name
            LightDevice light = (LightDevice) config.devices().getFirst();
            assertEquals("Legacy Light", light.name());
        } catch (Exception e) {
            fail("Legacy format test failed: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
