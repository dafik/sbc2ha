package iot.sbc2ha.hardware.profile;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import iot.sbc2ha.hardware.GpioChannel;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.HardwareModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HardwareProfileTest {

    @TempDir
    Path tempDir;

    private Path writeYaml(String content) {
        Path f = tempDir.resolve("profile.yaml");
        try {
            Files.writeString(f, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return f;
    }

    // --- Construction ---

    @Test
    void createsWithAllFields() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareProfile profile = new HardwareProfile("test-id", true, List.of(), List.of(ch), List.of(mapping));
        assertEquals("test-id", profile.id());
        assertTrue(profile.incomplete());
        assertEquals(0, profile.chips().size());
        assertEquals(1, profile.channelCount());
        assertEquals(1, profile.mappingCount());
    }

    @Test
    void incompleteFalseWhenNotSpecified() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(), List.of(ch), List.of());
        assertFalse(profile.incomplete());
    }

    @Test
    void incompleteNullDefaultsToFalse() {
        HardwareProfile profile = new HardwareProfile("test", null, List.of(), List.of(), List.of());
        assertFalse(profile.incomplete());
    }

    @Test
    void channelsNullSafe() {
        HardwareProfile profile = new HardwareProfile("test", false, List.of(), null, List.of());
        assertTrue(profile.channels().isEmpty());
    }

    @Test
    void mappingsNullSafe() {
        HardwareProfile profile = new HardwareProfile("test", false, List.of(), List.of(), null);
        assertTrue(profile.mappings().isEmpty());
    }

    // --- Collections are immutable ---

    @Test
    void channelsUnmodifiable() {
        HardwareProfile profile = new HardwareProfile("test", false, List.of(), List.of(), List.of());
        assertThrows(UnsupportedOperationException.class, () -> profile.channels().add(null));
    }

    @Test
    void mappingsUnmodifiable() {
        HardwareProfile profile = new HardwareProfile("test", false, List.of(), List.of(), List.of());
        assertThrows(UnsupportedOperationException.class, () -> profile.mappings().add(null));
    }

    // --- equals / hashCode ---

    @Test
    void equalsAndHashCode_sameContent() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareProfile a = new HardwareProfile("test", true, List.of(), List.of(ch), List.of());
        HardwareProfile b = new HardwareProfile("test", true, List.of(), List.of(ch), List.of());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentId() {
        HardwareProfile a = new HardwareProfile("a", true, List.of(), List.of(), List.of());
        HardwareProfile b = new HardwareProfile("b", true, List.of(), List.of(), List.of());
        assertNotEquals(a, b);
    }

    @Test
    void notEqual_differentIncomplete() {
        HardwareProfile a = new HardwareProfile("test", true, List.of(), List.of(), List.of());
        HardwareProfile b = new HardwareProfile("test", false, List.of(), List.of(), List.of());
        assertNotEquals(a, b);
    }

    // --- toString ---

    @Test
    void toStringIncludesAllFields() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareProfile profile = new HardwareProfile("test-id", true, List.of(), List.of(ch), List.of(mapping));
        String s = profile.toString();
        assertTrue(s.contains("test-id"));
        assertTrue(s.contains("true"));
        assertTrue(s.contains("channels=1"));
        assertTrue(s.contains("mappings=1"));
    }

    // --- YAML roundtrip ---

    @Test
    void yamlRoundTrip() throws Exception {
        String yaml = """
                id: yrt-test
                incomplete: true
                chips: []
                channels:
                  - type: gpio
                    pin: "P9_11"
                    direction: input
                mappings:
                  - logical_id: switch_x
                    description: "X switch"
                    physical:
                      type: gpio
                      pin: "P9_11"
                      direction: input
                """;
        Path f = writeYaml(yaml);
        JsonMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        HardwareProfile profile = mapper.readValue(f.toFile(), HardwareProfile.class);

        assertEquals("yrt-test", profile.id());
        assertTrue(profile.incomplete());
        assertEquals(1, profile.channelCount());
        assertEquals(1, profile.mappingCount());
    }

    // --- Expansion produces equivalent HardwareModel ---

    @Test
    void expansionEqualsManualModel() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_x", "X switch", ch);

        HardwareModel fromProfile = new HardwareModel("test", "test", List.of(), List.of(ch), List.of(mapping));
        HardwareModel fromManual = new HardwareModel("test", null, List.of(), List.of(ch), List.of(mapping));

        // Channels and mappings are equivalent even though profile differs
        assertEquals(fromProfile.channels(), fromManual.channels());
        assertEquals(fromProfile.mappings(), fromManual.mappings());
        assertSame(ch, fromProfile.getPhysicalChannel("switch_x"));
    }
}
