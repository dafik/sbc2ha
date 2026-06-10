package iot.sbc2ha.hardware;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import iot.sbc2ha.device.DeviceConfig;
import iot.sbc2ha.device.DeviceRegistry;
import iot.sbc2ha.device.OutputDevice;
import iot.sbc2ha.device.SwitchDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HardwareModelTest {

    @TempDir
    Path tempDir;

    private Path writeYaml(String content) {
        Path f = tempDir.resolve("hw.yaml");
        try {
            Files.writeString(f, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return f;
    }

    @Test
    void emptyModelByDefault() {
        HardwareModel model = new HardwareModel(null, null, null);
        assertNull(model.board());
        assertEquals(0, model.channelCount());
        assertEquals(0, model.mappingCount());
    }

    @Test
    void resolvesKnownLogicalId() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_entrance", "Entrance", ch);
        HardwareModel model = new HardwareModel("bone1", List.of(ch), List.of(mapping));
        assertSame(ch, model.getPhysicalChannel("switch_entrance"));
        assertSame(mapping, model.getMapping("switch_entrance"));
    }

    @Test
    void resolvesNullForUnknownLogicalId() {
        HardwareModel model = new HardwareModel("bone1", List.of(), List.of());
        assertNull(model.getPhysicalChannel("unknown"));
        assertNull(model.getMapping("unknown"));
    }

    @Test
    void resolveThrowsForUnknownLogicalId() {
        HardwareModel model = new HardwareModel("bone1", List.of(), List.of());
        HardwareMappingException ex = assertThrows(HardwareMappingException.class, () -> model.resolve("switch_x"));
        assertTrue(ex.getMessage().contains("No hardware mapping"));
    }

    @Test
    void validatePassesWhenAllDevicesMapped() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareModel model = new HardwareModel("bone1", List.of(ch), List.of(mapping));
        assertDoesNotThrow(() -> model.validate(Set.of("switch_1")));
    }

    @Test
    void validateFailsWhenDevicesMissing() {
        HardwareModel model = new HardwareModel("bone1", List.of(), List.of());
        HardwareMappingException ex = assertThrows(HardwareMappingException.class, () -> model.validate(Set.of("switch_1", "switch_2")));
        assertTrue(ex.getMessage().contains("Missing hardware mappings"));
        assertTrue(ex.getMessage().contains("switch_1"));
        assertTrue(ex.getMessage().contains("switch_2"));
    }

    @Test
    void validatePartialMissing() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareModel model = new HardwareModel("bone1", List.of(ch), List.of(mapping));
        HardwareMappingException ex = assertThrows(HardwareMappingException.class, () -> model.validate(Set.of("switch_1", "switch_missing")));
        assertTrue(ex.getMessage().contains("switch_missing"));
    }

    @Test
    void channelsAndMappingsAreUnmodifiable() {
        HardwareModel model = new HardwareModel("bone1", List.of(), List.of());
        assertThrows(UnsupportedOperationException.class, () -> addToList(model.channels()));
        assertThrows(UnsupportedOperationException.class, () -> addToList(model.mappings()));
    }

    private static <T> void addToList(List<T> list) {
        list.add(null);
    }

    @Test
    void boardIsSet() {
        HardwareModel model = new HardwareModel("bone1", List.of(), List.of());
        assertEquals("bone1", model.board());
    }

    @Test
    void integrationWithDeviceRegistry_mapsOutputs() {
        GpioChannel switchCh = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        Mcp23017Channel outCh = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        HardwareMapping switchMapping = new HardwareMapping("switch_entrance", "Entrance", switchCh);
        HardwareMapping outMapping = new HardwareMapping("out_relay1", "Relay 1", outCh);
        HardwareModel model = new HardwareModel("bone1", List.of(switchCh, outCh), List.of(switchMapping, outMapping));

        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_entrance", "Entrance", "out_relay1"));
        reg.add(new OutputDevice("out_relay1", "Relay 1"));
        reg.validate();

        // Validate that all devices have hardware mappings
        assertDoesNotThrow(() -> model.validate(
                reg.all().stream().map(DeviceConfig::id).collect(java.util.stream.Collectors.toSet())));

        // Verify resolution works
        assertSame(switchCh, model.resolve("switch_entrance"));
        assertSame(outCh, model.resolve("out_relay1"));
    }

    @Test
    void yamRoundTrip() throws Exception {
        String yaml = """
                board: bone1
                channels:
                  - type: gpio
                    location: "P9_11"
                    direction: input
                mappings:
                  - logical_id: switch_entrance
                    description: "Entrance door switch"
                    physical:
                      type: gpio
                      location: "P9_11"
                      direction: input
                """;
        Path f = writeYaml(yaml);
        JsonMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        HardwareModel model = mapper.readValue(f.toFile(), HardwareModel.class);
        assertEquals("bone1", model.board());
        assertEquals(1, model.channelCount());
        assertEquals(1, model.mappingCount());

        PhysicalChannel ch = model.channels().getFirst();
        assertInstanceOf(GpioChannel.class, ch);
        GpioChannel gpio = (GpioChannel) ch;
        assertEquals("P9_11", gpio.location());
        assertEquals(GpioChannel.Direction.INPUT, gpio.direction());

        HardwareMapping mapping = model.mappings().getFirst();
        assertEquals("switch_entrance", mapping.logicalId());
        assertEquals(ch, mapping.physical());
    }

    @Test
    void equalsAndHashCode() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareModel a = new HardwareModel("bone1", List.of(ch), List.of(mapping));
        HardwareModel b = new HardwareModel("bone1", List.of(ch), List.of(mapping));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentBoard() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareModel a = new HardwareModel("bone1", List.of(ch), List.of());
        HardwareModel b = new HardwareModel("bone2", List.of(ch), List.of());
        assertNotEquals(a, b);
    }

    @Test
    void profileFieldSetAndAccessed() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareModel model = new HardwareModel("bone1", "boneio.input-v0.3", List.of(ch), List.of());
        assertEquals("boneio.input-v0.3", model.profile());
        assertEquals("bone1", model.board());
    }

    @Test
    void profileNullWhenNotSet() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareModel model = new HardwareModel("bone1", List.of(ch), List.of());
        assertNull(model.profile());
    }

    @Test
    void profileIncludedInEqualsAndHashCode() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        var mappings = List.<HardwareMapping>of();
        HardwareModel a = new HardwareModel("bone1", "profile-a", List.of(ch), mappings);
        HardwareModel b = new HardwareModel("bone1", "profile-a", List.of(ch), mappings);
        HardwareModel c = new HardwareModel("bone1", "profile-b", List.of(ch), mappings);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }
}
