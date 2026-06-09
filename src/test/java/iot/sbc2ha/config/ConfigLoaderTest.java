package iot.sbc2ha.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    private Path writeYaml(String content) throws Exception {
        Path f = tempDir.resolve("test.yaml");
        Files.writeString(f, content);
        return f;
    }

    @Test
    void loadsValidConfig() throws Exception {
        Sbc2haConfig cfg = ConfigLoader.load(writeYaml("""
                node_id: bbb-core-1
                schema: "1"
                """));
        assertEquals("bbb-core-1", cfg.nodeId());
        assertEquals("1", cfg.schema());
    }

    @Test
    void loadsFromPathString() throws Exception {
        Sbc2haConfig cfg = ConfigLoader.load(writeYaml("""
                node_id: test-node
                schema: "1"
                """).toString());
        assertEquals("test-node", cfg.nodeId());
    }

    @Test
    void failsOnMissingNode() {
        assertThrows(ValidationException.class, () -> ConfigLoader.load(writeYaml("""
                schema: "1"
                """)));
    }

    @Test
    void failsOnMissingSchema() {
        assertThrows(ValidationException.class, () -> ConfigLoader.load(writeYaml("""
                node_id: test-node
                """)));
    }

    @Test
    void failsOnInvalidNodeId() {
        assertThrows(ValidationException.class, () -> ConfigLoader.load(writeYaml("""
                node_id: UPPER-CASE
                schema: "1"
                """)));
    }

    @Test
    void failsOnMissingFile() {
        assertThrows(ValidationException.class, () -> ConfigLoader.load("/nonexistent/path.yaml"));
    }

    @Test
    void failsOnMalformedYaml() {
        assertThrows(ValidationException.class, () -> ConfigLoader.load(writeYaml("::invalid{yaml")));
    }
}
