package iot.sbc2ha.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test: load .plan/config/bone1-converted.yaml and verify device counts.
 */
class Bone1ConvertedTest {

    @Test
    void loadsCorrectly() {
        Sbc2haConfig config = ConfigLoader.load(".plan/config/bone1-converted.yaml");
        assertEquals("boneio-1", config.nodeId());
        assertEquals("1", config.schema());
    }

    @Test
    void deviceCounts() {
        Sbc2haConfig config = ConfigLoader.load(".plan/config/bone1-converted.yaml");
        var all = config.devices();
        assertEquals(65, all.size());

        long switches = all.stream().filter(d -> d.type() == iot.sbc2ha.device.DeviceConfig.DeviceType.SWITCH).count();
        long lights = all.stream().filter(d -> d.type() == iot.sbc2ha.device.DeviceConfig.DeviceType.LIGHT).count();
        long inputs = all.stream().filter(d -> d.type() == iot.sbc2ha.device.DeviceConfig.DeviceType.INPUT).count();

        assertEquals(32, switches);
        assertEquals(23, lights);
        assertEquals(10, inputs);
    }

    @Test
    void registryValidates() {
        Sbc2haConfig config = ConfigLoader.load(".plan/config/bone1-converted.yaml");
        // validate() builds the registry — should not throw
        assertDoesNotThrow(config::validate);
        assertNotNull(config.registry());
    }

    @Test
    void switchActionsHaveTargets() {
        Sbc2haConfig config = ConfigLoader.load(".plan/config/bone1-converted.yaml");
        for (var dev : config.devices()) {
            if (dev instanceof iot.sbc2ha.device.SwitchDevice sw) {
                var actions = sw.actions();
                if (actions != null) {
                    for (var entry : actions.entrySet()) {
                        for (var mapping : entry.getValue()) {
                            assertNotNull(mapping.target(),
                                    "Switch '" + dev.id() + "' event '" + entry.getKey() + "' has null target");
                            assertEquals(iot.sbc2ha.device.ActionMapping.ActionType.OUTPUT_TOGGLE, mapping.type(),
                                    "Switch '" + dev.id() + "' event '" + entry.getKey() + "' has unexpected type");
                        }
                    }
                }
            }
        }
    }
}
