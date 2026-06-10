package iot.sbc2ha.hardware.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DebugProfileLoadTest {

    @Test
    void debugLoad() {
        ProfileRegistry registry = new ProfileRegistry();
        HardwareProfile loaded = registry.loadFromClasspath("hardware-profiles/boneio/input-v0.3.yaml");
        System.out.println("DEBUG: id=" + loaded.id());
        System.out.println("DEBUG: incomplete=" + loaded.incomplete());
        System.out.println("DEBUG: channels=" + loaded.channelCount());
        System.out.println("DEBUG: mappings=" + loaded.mappingCount());
        assertFalse(loaded.incomplete(), "incomplete should be false");
    }
}
