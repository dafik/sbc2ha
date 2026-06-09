package iot.sbc2ha.hardware.profile;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.Mcp23017Channel;
import iot.sbc2ha.hardware.GpioChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileRegistryTest {

    private ProfileRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ProfileRegistry();
    }

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

    // --- Registration ---

    @Test
    void registerAndHasProfile() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(ch), List.of(mapping));
        registry.register(profile);
        assertTrue(registry.hasProfile("test"));
        assertEquals(1, registry.profileCount());
        assertTrue(registry.registeredProfileNames().contains("test"));
    }

    @Test
    void registerDuplicateThrows() {
        HardwareProfile p1 = new HardwareProfile("test", false, List.of(), List.of());
        registry.register(p1);
        HardwareProfile p2 = new HardwareProfile("test", false, List.of(), List.of());
        ProfileLoadingException ex = assertThrows(ProfileLoadingException.class, () -> registry.register(p2));
        assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    void registerNullProfileThrows() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    // --- Expand ---

    @Test
    void expandPreservesChannels() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(ch), List.of(mapping));
        registry.register(profile);

        HardwareModel model = registry.expand("test", null, null);
        assertEquals("test", model.board());
        assertEquals("test", model.profile());
        assertEquals(1, model.channelCount());
        assertEquals(1, model.mappingCount());
        assertSame(ch, model.channels().getFirst());
    }

    @Test
    void expandWithExtraAppends() {
        GpioChannel ch1 = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(ch1), List.of());
        registry.register(profile);

        GpioChannel ch2 = new GpioChannel("P9_12", GpioChannel.Direction.INPUT);
        HardwareModel model = registry.expand("test", List.of(ch2), null);
        assertEquals(2, model.channelCount());
    }

    @Test
    void expandWithExtraMappingsAppends() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping m1 = new HardwareMapping("switch_1", "switch 1", ch);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(ch), List.of(m1));
        registry.register(profile);

        HardwareMapping m2 = new HardwareMapping("switch_2", "switch 2", ch);
        HardwareModel model = registry.expand("test", null, List.of(m2));
        assertEquals(2, model.mappingCount());
    }

    @Test
    void expandUnknownProfileThrows() {
        ProfileLoadingException ex = assertThrows(ProfileLoadingException.class, () -> registry.expand("unknown", null, null));
        assertTrue(ex.getMessage().contains("Unknown profile"));
        assertTrue(ex.getMessage().contains("unknown"));
    }

    // --- Override ---

    @Test
    void expandWithOverridesReplacesChannels() {
        GpioChannel profileCh = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel overrideCh = new GpioChannel("P9_11", GpioChannel.Direction.OUTPUT);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(profileCh), List.of());
        registry.register(profile);

        HardwareModel model = registry.expandWithOverrides("test", List.of(overrideCh), null);
        assertEquals(1, model.channelCount());
        assertSame(overrideCh, model.channels().getFirst());
    }

    @Test
    void expandWithOverridesReplacesMappings() {
        GpioChannel profileCh = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel overrideCh = new GpioChannel("P9_12", GpioChannel.Direction.OUTPUT);
        HardwareMapping profileMapping = new HardwareMapping("switch_1", "switch 1", profileCh);
        HardwareMapping overrideMapping = new HardwareMapping("switch_1", "switch 1 OVER", overrideCh);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(profileCh), List.of(profileMapping));
        registry.register(profile);

        HardwareModel model = registry.expandWithOverrides("test", null, List.of(overrideMapping));
        HardwareMapping resolved = model.getMapping("switch_1");
        assertSame(overrideMapping, resolved);
    }

    @Test
    void expandWithOverridesAddsNew() {
        GpioChannel ch1 = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel ch2 = new GpioChannel("P9_99", GpioChannel.Direction.INPUT);
        HardwareProfile profile = new HardwareProfile("test", false, List.of(ch1), List.of());
        registry.register(profile);

        HardwareModel model = registry.expandWithOverrides("test", List.of(ch2), null);
        assertEquals(2, model.channelCount());
        assertTrue(model.channels().stream().anyMatch(c -> "P9_99".equals(c.location())));
    }

    @Test
    void expandWithOverridesUnknownProfileThrows() {
        assertThrows(ProfileLoadingException.class, () -> registry.expandWithOverrides("unknown", List.of(), List.of()));
    }

    // --- Load from classpath ---

    @Test
    void loadFromClasspathSuccess() {
        HardwareProfile loaded = registry.loadFromClasspath("hardware-profiles/boneio/input-v0.3.yaml");
        assertEquals("boneio.input-v0.3", loaded.id());
        assertTrue(loaded.incomplete());
        assertEquals(2, loaded.channelCount());
        assertEquals(2, loaded.mappingCount());
    }

    @Test
    void loadFromClasspathNotFoundThrows() {
        ProfileLoadingException ex = assertThrows(ProfileLoadingException.class,
                () -> registry.loadFromClasspath("nonexistent/profile.yaml"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // --- Load from file ---

    @Test
    void loadFromFileSuccess() {
        String yaml = """
                id: file-profile
                incomplete: true
                channels:
                  - type: gpio
                    location: "P8_07"
                    direction: input
                mappings:
                  - logical_id: input_1
                    description: "File input"
                    physical:
                      type: gpio
                      location: "P8_07"
                      direction: input
                """;
        Path f = writeYaml(yaml);
        HardwareProfile loaded = registry.loadFromFile(f);
        assertEquals("file-profile", loaded.id());
        assertTrue(loaded.incomplete());
        assertEquals(1, loaded.channelCount());
    }

    // --- Equivalence ---

    @Test
    void profileExpansionEquivalentToManual() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("switch_entrance", "Entrance door", ch);

        // Register profile
        HardwareProfile profile = new HardwareProfile("equiv-test", false, List.of(ch), List.of(mapping));
        registry.register(profile);

        // Expand from profile
        HardwareModel fromProfile = registry.expand("equiv-test", null, null);

        // Create manual model with same channels and mappings
        HardwareModel fromManual = new HardwareModel(
                "equiv-test", null,
                List.of(ch),
                List.of(mapping));

        // Functional equivalence: same board, channels, mappings, and resolution
        assertEquals(fromManual.board(), fromProfile.board());
        assertEquals(fromManual.channels(), fromProfile.channels());
        assertEquals(fromManual.mappings(), fromProfile.mappings());
        assertSame(ch, fromProfile.getPhysicalChannel("switch_entrance"));
        assertSame(mapping, fromProfile.getMapping("switch_entrance"));
    }

    // --- YAML roundtrip ---

    @Test
    void hardwareProfileYamlRoundTrip() throws Exception {
        String yaml = """
                id: roundtrip-test
                incomplete: true
                channels:
                  - type: mcp23017
                    location: "i2c-1:0x20:A:0"
                    port: A
                    pin: 0
                mappings:
                  - logical_id: out_1
                    description: "First relay"
                    physical:
                      type: mcp23017
                      location: "i2c-1:0x20:A:0"
                      port: A
                      pin: 0
                """;
        Path f = writeYaml(yaml);
        JsonMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        HardwareProfile profile = mapper.readValue(f.toFile(), HardwareProfile.class);

        assertEquals("roundtrip-test", profile.id());
        assertTrue(profile.incomplete());
        assertEquals(1, profile.channelCount());
        assertEquals(1, profile.mappingCount());
        assertInstanceOf(Mcp23017Channel.class, profile.channels().getFirst());
        Mcp23017Channel mcp = (Mcp23017Channel) profile.channels().getFirst();
        assertEquals(Mcp23017Channel.Port.A, mcp.port());
        assertEquals(0, mcp.pin());
    }

    // --- equals / hashCode ---

    @Test
    void hardwareProfileEqualsAndHashCode() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareProfile a = new HardwareProfile("test", false, List.of(ch), List.of());
        HardwareProfile b = new HardwareProfile("test", false, List.of(ch), List.of());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hardwareProfileNotEqual_differentId() {
        HardwareProfile a = new HardwareProfile("test-a", false, List.of(), List.of());
        HardwareProfile b = new HardwareProfile("test-b", false, List.of(), List.of());
        assertNotEquals(a, b);
    }

    @Test
    void hardwareProfileNotEqual_differentIncomplete() {
        HardwareProfile a = new HardwareProfile("test", true, List.of(), List.of());
        HardwareProfile b = new HardwareProfile("test", false, List.of(), List.of());
        assertNotEquals(a, b);
    }

    // --- toString ---

    @Test
    void hardwareProfileToStringIncludesFields() {
        HardwareProfile profile = new HardwareProfile("test", true, List.of(), List.of());
        String s = profile.toString();
        assertTrue(s.contains("test"));
        assertTrue(s.contains("incomplete=true"));
    }

    @Test
    void profileRegistryToString() {
        HardwareProfile profile = new HardwareProfile("test", false, List.of(), List.of());
        registry.register(profile);
        String s = registry.toString();
        assertTrue(s.contains("test"));
    }
}
