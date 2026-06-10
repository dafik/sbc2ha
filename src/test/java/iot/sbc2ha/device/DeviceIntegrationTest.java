package iot.sbc2ha.device;

import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.config.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeviceIntegrationTest {

    @TempDir
    Path tempDir;

    private Path writeYaml(String content) {
        Path f = tempDir.resolve("test.yaml");
        try {
            Files.writeString(f, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return f;
    }

    @Test
    void validConfigWithDevices_loadsCorrectly() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                devices:
                  - type: light
                    id: light_kitchen
                    display_name: Kitchen light
                  - type: switch
                    id: switch_entrance
                    display_name: Entrance
                    click_action: light_kitchen
                """;
        Sbc2haConfig config = iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml));
        assertEquals("bbb-core-1", config.nodeId());
        assertEquals("1", config.schema());
        assertEquals(2, config.devices().size());
    }

    @Test
    void configWithUnknownClickActionTarget_throws() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                devices:
                  - type: switch
                    id: switch_entrance
                    click_action: light_missing
                """;
        assertThrows(ValidationException.class, () -> iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml)));
    }

    @Test
    void configWithDuplicateDeviceIds_throws() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                devices:
                  - type: light
                    id: light_1
                  - type: switch
                    id: light_1
                    click_action: light_1
                """;
        assertThrows(ValidationException.class, () -> iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml)));
    }

    @Test
    void configWithOutputDevice_loadsCorrectly() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                devices:
                  - type: output
                    id: out_relay1
                    display_name: Relay 1
                  - type: switch
                    id: switch_entrance
                    click_action: out_relay1
                """;
        Sbc2haConfig config = iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml));
        assertEquals(2, config.devices().size());
    }

    @Test
    void configWithNoDevices_loadsCorrectly() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                devices: []
                """;
        Sbc2haConfig config = iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml));
        assertEquals(0, config.devices().size());
    }

    @Test
    void configWithNoDevicesField_loadsCorrectly() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                """;
        Sbc2haConfig config = iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml));
        assertEquals(0, config.devices().size());
    }

    @Test
    void configWithInputDevice_loadsCorrectly() {
        String yaml = """
                node_id: bbb-core-1
                schema: "1"
                devices:
                  - type: input
                    id: door_entrance
                    display_name: Entrance door
                    sensor_type: door
                  - type: input
                    id: motion_kitchen
                    display_name: Kitchen motion
                    sensor_type: motion
                    inverted: true
                """;
        Sbc2haConfig config = iot.sbc2ha.config.ConfigLoader.load(writeYaml(yaml));
        assertEquals(2, config.devices().size());
        assertInstanceOf(InputDevice.class, config.devices().getFirst());
        assertInstanceOf(InputDevice.class, config.devices().getLast());

        InputDevice door = (InputDevice) config.devices().getFirst();
        assertEquals("door_entrance", door.id());
        assertEquals(InputDevice.SensorType.DOOR, door.sensorType());
        assertFalse(door.inverted());

        InputDevice motion = (InputDevice) config.devices().getLast();
        assertEquals("motion_kitchen", motion.id());
        assertEquals(InputDevice.SensorType.MOTION, motion.sensorType());
        assertTrue(motion.inverted());
    }
}
