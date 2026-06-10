package iot.sbc2ha.config;

import iot.sbc2ha.device.DeviceConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.LightDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validate that bone1 (50 inputs) and bone2 (24 outputs) planning configs
 * load successfully and that the device model handles scale.
 * <p>
 * These configs are DRAFT — used for scale check, not production.
 * <p>
 * Pain points documented from drafting:
 * <ol>
 *   <li>Repetitive device entries: no config template/loop mechanism</li>
 *   <li>Per-device expose.ha.events duplication across many switches</li>
 *   <li>Actions.click wiring is verbose for simple toggle patterns</li>
 *   <li>No config template or "default" mechanism exists</li>
 *   <li>Location grouping by floor is implicit (no programmatic floor query)</li>
 * </ol>
 */
class Sbc013PlanningConfigsTest {

    @TempDir
    Path tempDir;

    private Sbc2haConfig loadResource(String resourcePath) throws IOException {
        try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Resource not found on classpath: " + resourcePath);
            Path target = tempDir.resolve(resourcePath.replace('/', '_'));
            Files.copy(is, target);
            return ConfigLoader.load(target.toString());
        }
    }

    @Test
    void bone1_50inputs_loads_and_validates() throws IOException {
        Sbc2haConfig cfg = loadResource("bone1-planning-50inputs.yaml");

        assertEquals("bone1", cfg.nodeId());
        assertEquals("1", cfg.schema());
        assertTrue(cfg.runtime().isOfflineFirst());

        cfg.validate();
        DeviceRegistry reg = cfg.registry();
        assertNotNull(reg);

        int deviceCount = cfg.devices().size();

        // bone1-planning-50inputs.yaml has exactly 50 device entries
        assertEquals(50, deviceCount, "bone1 should have 50 devices for scale check");

        // Count by type
        long switchCount = cfg.devices().stream().filter(d -> d.type() == DeviceConfig.DeviceType.SWITCH).count();
        long lightCount = cfg.devices().stream().filter(d -> d.type() == DeviceConfig.DeviceType.LIGHT).count();
        long inputCount = cfg.devices().stream().filter(d -> d.type() == DeviceConfig.DeviceType.INPUT).count();

        // Verify type breakdown
        assertTrue(switchCount >= 10, "Expected >= 10 switches, got " + switchCount);
        assertTrue(lightCount >= 10, "Expected >= 10 lights, got " + lightCount);
        assertTrue(inputCount >= 20, "Expected >= 20 input/sensor devices, got " + inputCount);

        // Verify locations use dotted notation
        Map<String, LocationConfig> locations = cfg.locations();
        assertTrue(locations.containsKey("building.floor1.stairs"));
        assertTrue(locations.containsKey("building.floor2.hallway"));
        assertTrue(locations.containsKey("building.floor2.lobby"));

        // Verify parent references
        LocationConfig stairs = locations.get("building.floor1.stairs");
        assertNotNull(stairs);
        assertEquals("building.floor1", stairs.parent());

        // Verify all devices have valid location references
        for (DeviceConfig dev : cfg.devices()) {
            assertNotNull(dev.location(), "Device " + dev.id() + " must have a location");
            assertTrue(locations.containsKey(dev.location()),
                    "Device " + dev.id() + " location '" + dev.location() + "' not in locations map");
        }

        // Verify actions exist on switches
        long switchesWithActions = cfg.devices().stream()
                .filter(d -> d.type() == DeviceConfig.DeviceType.SWITCH)
                .filter(d -> ((iot.sbc2ha.device.SwitchDevice) d).actions() != null)
                .count();
        assertEquals(switchCount, switchesWithActions, "All switches should have actions configured");

        System.out.println("bone1-planning-50inputs.yaml: " + deviceCount + " devices ("
                + switchCount + " switches, " + lightCount + " lights, " + inputCount + " inputs)");
    }

    @Test
    void bone2_24outputs_loads_and_validates() throws IOException {
        Sbc2haConfig cfg = loadResource("bone2-planning-24outputs.yaml");

        assertEquals("bone2", cfg.nodeId());
        assertEquals("1", cfg.schema());
        assertTrue(cfg.runtime().isOfflineFirst());

        cfg.validate();
        DeviceRegistry reg = cfg.registry();
        assertNotNull(reg);

        int deviceCount = cfg.devices().size();

        // bone2-planning-24outputs.yaml has exactly 24 device entries
        assertEquals(24, deviceCount, "bone2 should have 24 devices for scale check");

        // All should be lights (output devices)
        long lightCount = cfg.devices().stream().filter(d -> d.type() == DeviceConfig.DeviceType.LIGHT).count();
        assertEquals(24, lightCount, "All bone2 devices should be lights");

        // Verify all have restore_state
        for (DeviceConfig dev : cfg.devices()) {
            LightDevice light = (LightDevice) dev;
            assertTrue(light.restoreState(), "Device " + dev.id() + " should have restore_state=true");
        }

        // Verify locations use dotted notation
        Map<String, LocationConfig> locations = cfg.locations();
        assertTrue(locations.containsKey("building2.floor1.zone_a"));
        assertTrue(locations.containsKey("building2.floor2.zone_f"));

        System.out.println("bone2-planning-24outputs.yaml: " + deviceCount + " devices ("
                + lightCount + " lights)");
    }

    @Test
    void bone1_output_references_exist() throws IOException {
        // Verify that bone1's output_board references resolve to expected output ranges
        Sbc2haConfig cfg = loadResource("bone1-planning-50inputs.yaml");
        cfg.validate();

        List<String> outputsUsed = cfg.devices().stream()
                .map(DeviceConfig::output)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // bone1 uses outputs 1-11 (lights across 2 floors)
        assertEquals(11, outputsUsed.size(), "bone1 should reference 11 distinct outputs");
        for (String ref : outputsUsed) {
            assertTrue(ref.startsWith("output_board.output"), "Output ref should use profile channel syntax: " + ref);
        }
    }

    @Test
    void bone2_output_references_span_1_to_24() throws IOException {
        // Verify that bone2's output references span the full 1-24 range
        Sbc2haConfig cfg = loadResource("bone2-planning-24outputs.yaml");
        cfg.validate();

        List<String> outputsUsed = cfg.devices().stream()
                .map(DeviceConfig::output)
                .filter(Objects::nonNull)
                .distinct()
                .sorted((a, b) -> {
                    // Extract numeric part for natural-order comparison
                    int numA = Integer.parseInt(a.replace("output_board.output", ""));
                    int numB = Integer.parseInt(b.replace("output_board.output", ""));
                    return Integer.compare(numA, numB);
                })
                .toList();

        assertEquals(24, outputsUsed.size(), "bone2 should reference all 24 outputs");
        for (int i = 1; i <= 24; i++) {
            assertEquals("output_board.output" + i, outputsUsed.get(i - 1),
                    "Output " + i + " should be referenced");
        }
    }
}
