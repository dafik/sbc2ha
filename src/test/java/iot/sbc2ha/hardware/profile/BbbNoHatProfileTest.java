package iot.sbc2ha.hardware.profile;

import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.PhysicalChannel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-level test that loads the BBB bare-board profile from
 * classpath YAML and validates its structure.
 */
class BbbNoHatProfileTest {

    private static final String BBB_NOHAT_PROFILE = "hardware-profiles/bbb/nohat.yaml";

    @Test
    void loadsFromClasspath() {
        ProfileRegistry registry = new ProfileRegistry();
        HardwareProfile profile = registry.loadFromClasspath(BBB_NOHAT_PROFILE);
        assertEquals("bbb.nohat", profile.id());
        assertFalse(profile.incomplete());
    }

    @Test
    void expandsToHardwareModel() {
        ProfileRegistry registry = new ProfileRegistry();
        HardwareProfile profile = registry.loadFromClasspath(BBB_NOHAT_PROFILE);
        registry.register(profile);

        HardwareModel model = registry.expand("bbb.nohat", null, null);
        assertEquals("bbb.nohat", model.board());
        assertTrue(model.channelCount() > 0);
        assertTrue(model.mappingCount() > 0);
    }

    @Test
    void hasCorrectPinCounts() {
        ProfileRegistry registry = new ProfileRegistry();
        HardwareProfile profile = registry.loadFromClasspath(BBB_NOHAT_PROFILE);
        registry.register(profile);

        HardwareModel model = registry.expand("bbb.nohat", null, null);
        // 44 P8 + 19 P9 + 7 ADC = 70 channels
        assertEquals(70, model.channelCount());
        // 63 outputs + 7 analog inputs = 70 mappings
        assertEquals(70, model.mappingCount());
    }

    @Test
    void pinLocationsAreUnique() {
        ProfileRegistry registry = new ProfileRegistry();
        HardwareProfile profile = registry.loadFromClasspath(BBB_NOHAT_PROFILE);
        registry.register(profile);

        HardwareModel model = registry.expand("bbb.nohat", null, null);
        List<String> locations = model.channels().stream()
                .map(PhysicalChannel::location)
                .toList();
        assertEquals(locations.size(), locations.stream().distinct().count(),
                "Duplicate pin locations in profile");
    }

    @Test
    void pinLocationsIncludeAllP8P9() {
        ProfileRegistry registry = new ProfileRegistry();
        HardwareProfile profile = registry.loadFromClasspath(BBB_NOHAT_PROFILE);
        registry.register(profile);

        HardwareModel model = registry.expand("bbb.nohat", null, null);
        List<String> locations = model.channels().stream()
                .map(PhysicalChannel::location)
                .toList();

        // Spot-check some key BBB pins
        assertTrue(locations.contains("P8_03"));
        assertTrue(locations.contains("P8_46"));
        assertTrue(locations.contains("P9_11"));
        assertTrue(locations.contains("P9_42"));
        assertTrue(locations.contains("P9_33"));  // ADC
    }
}
