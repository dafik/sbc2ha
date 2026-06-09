package iot.sbc2ha.hardware;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoardProfileTest {

    @Test
    void createsWithAllFields() {
        var desc = Map.of("board", "BBB + MCP23017 + SH1106", "notes", "First BoneIO revision");
        BoardProfile profile = new BoardProfile("bone1", "beaglebone-black", desc);
        assertEquals("bone1", profile.name());
        assertEquals("beaglebone-black", profile.platform());
        assertEquals(2, profile.description().size());
        assertEquals("BBB + MCP23017 + SH1106", profile.description().get("board"));
    }

    @Test
    void descriptionNullSafe() {
        BoardProfile profile = new BoardProfile("bone1", "beaglebone-black", null);
        assertTrue(profile.description().isEmpty());
    }

    @Test
    void descriptionIsCopy() {
        var desc = Map.of("board", "BBB");
        BoardProfile profile = new BoardProfile("bone1", "bbb", desc);
        assertThrows(UnsupportedOperationException.class, () -> profile.description().put("x", "y"));
    }

    @Test
    void equalsAndHashCode_sameContent() {
        var desc = Map.of("board", "BBB");
        BoardProfile a = new BoardProfile("bone1", "bbb", desc);
        BoardProfile b = new BoardProfile("bone1", "bbb", desc);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringIncludesNameAndPlatform() {
        BoardProfile profile = new BoardProfile("bone1", "beaglebone-black", Map.of());
        String s = profile.toString();
        assertTrue(s.contains("bone1"));
        assertTrue(s.contains("beaglebone-black"));
    }
}
