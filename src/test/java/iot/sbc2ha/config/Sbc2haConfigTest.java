package iot.sbc2ha.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Sbc2haConfigTest {

    @Test
    void validConfig() {
        Sbc2haConfig cfg = new Sbc2haConfig("bbb-core-1", "1");
        assertDoesNotThrow(cfg::validate);
        assertEquals("bbb-core-1", cfg.nodeId());
        assertEquals("1", cfg.schema());
    }

    @Test
    void nodeId_accepts_dots_dashes_underscores() {
        var ids = new String[]{"a", "a.b", "a-b", "a_b", "node_1.internal-test"};
        for (String id : ids) {
            Sbc2haConfig cfg = new Sbc2haConfig(id, "1");
            assertDoesNotThrow(cfg::validate, () -> "failed for nodeId: " + id);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "A", "1A", "a b", "a/b", ".start", "-start", "_start"})
    void nodeId_rejects_invalid(String invalidNodeId) {
        Sbc2haConfig cfg = new Sbc2haConfig(invalidNodeId, "1");
        ValidationException ex = assertThrows(ValidationException.class, cfg::validate);
        // null -> "node_id is required"; others -> "must match"
        assertTrue(ex.getMessage().contains("node_id"), () -> "Expected 'node_id' in: " + ex.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "2", "beta", "1.1"})
    void schema_rejects_unsupported(String invalidSchema) {
        Sbc2haConfig cfg = new Sbc2haConfig("valid-id", invalidSchema);
        ValidationException ex = assertThrows(ValidationException.class, cfg::validate);
        assertTrue(ex.getMessage().contains("schema"), () -> "Expected 'schema' in: " + ex.getMessage());
    }

    @Test
    void nodeId_missing() {
        Sbc2haConfig cfg = new Sbc2haConfig();
        ValidationException ex = assertThrows(ValidationException.class, cfg::validate);
        assertTrue(ex.getMessage().contains("node_id is required"));
    }

    @Test
    void schema_missing() {
        Sbc2haConfig cfg = new Sbc2haConfig();
        cfg.setNodeId("bbb-core-1");
        ValidationException ex = assertThrows(ValidationException.class, cfg::validate);
        assertTrue(ex.getMessage().contains("schema is required"));
    }

    @Test
    void equalsAndHashCode() {
        Sbc2haConfig a = new Sbc2haConfig("bbb", "1");
        Sbc2haConfig b = new Sbc2haConfig("bbb", "1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentNodeId() {
        assertNotEquals(new Sbc2haConfig("a", "1"), new Sbc2haConfig("b", "1"));
    }

    @Test
    void notEqual_differentSchema() {
        assertNotEquals(new Sbc2haConfig("a", "1"), new Sbc2haConfig("a", "2"));
    }
}
