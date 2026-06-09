package iot.sbc2ha.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StateService}.
 */
class StateServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void noStateFile_returnsEmptyMap() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        Map<String, DeviceState> state = service.load();

        assertTrue(state.isEmpty());
    }

    @Test
    void persistAndRestore() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        service.setState("out_1", DeviceState.ON);
        service.setState("out_2", DeviceState.OFF);

        // Create a new instance to simulate restart
        StateService fresh = new StateService(stateFile);
        Map<String, DeviceState> restored = fresh.load();

        assertEquals(2, restored.size());
        assertEquals(DeviceState.ON, restored.get("out_1"));
        assertEquals(DeviceState.OFF, restored.get("out_2"));
    }

    @Test
    void persist_multipleUpdates() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        service.setState("light_1", DeviceState.ON);
        service.setState("light_1", DeviceState.OFF);
        service.setState("light_1", DeviceState.ON);

        StateService fresh = new StateService(stateFile);
        Map<String, DeviceState> restored = fresh.load();

        assertEquals(1, restored.size());
        assertEquals(DeviceState.ON, restored.get("light_1"));
    }

    @Test
    void getState_returnsPersistedValue() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        service.setState("out_1", DeviceState.ON);
        assertEquals(DeviceState.ON, service.getState("out_1"));

        service.setState("out_1", DeviceState.OFF);
        assertEquals(DeviceState.OFF, service.getState("out_1"));
    }

    @Test
    void getState_unknownDevice_returnsNull() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        assertNull(service.getState("nonexistent"));
    }

    @Test
    void corruptFile_returnsEmptyMap() throws IOException {
        Path stateFile = tempDir.resolve("state.json");
        Files.writeString(stateFile, "this is not json {{{");

        StateService service = new StateService(stateFile);

        Map<String, DeviceState> state = service.load();
        assertTrue(state.isEmpty());
    }

    @Test
    void corruptedStateFile_returnsEmptyMap() throws IOException {
        Path stateFile = tempDir.resolve("state.json");
        // Valid JSON but not matching the expected structure
        Files.writeString(stateFile, "{\"foo\": \"bar\"}");

        StateService service = new StateService(stateFile);

        Map<String, DeviceState> state = service.load();
        assertTrue(state.isEmpty());
    }

    @Test
    void wrongVersion_returnsEmptyMap() throws IOException {
        Path stateFile = tempDir.resolve("state.json");
        String json = "{\"version\": \"99\", \"map\": {\"out_1\": \"ON\"}}";
        Files.writeString(stateFile, json);

        StateService service = new StateService(stateFile);

        Map<String, DeviceState> state = service.load();
        assertTrue(state.isEmpty());
    }

    @Test
    void getAllStates_returnsAll() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        service.setState("out_1", DeviceState.ON);
        service.setState("out_2", DeviceState.OFF);
        service.setState("light_1", DeviceState.ON);

        Map<String, DeviceState> all = service.getAllStates();
        assertEquals(3, all.size());
        assertEquals(DeviceState.ON, all.get("out_1"));
        assertEquals(DeviceState.OFF, all.get("out_2"));
        assertEquals(DeviceState.ON, all.get("light_1"));
    }

    @Test
    void getAllStates_unmodifiable() {
        Path stateFile = tempDir.resolve("state.json");
        StateService service = new StateService(stateFile);

        service.setState("out_1", DeviceState.ON);

        Map<String, DeviceState> all = service.getAllStates();
        // Intentional: verify UnsupportedOperationException is thrown
        assertThrows(UnsupportedOperationException.class, () -> all.put("out_2", DeviceState.OFF));
    }
}
