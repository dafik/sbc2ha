package iot.sbc2ha.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent state store for togglable devices.
 *
 * <p>State is persisted as JSON to disk using atomic write (write to
 * temporary file, then rename). State keys are stable device IDs.
 * Names are never used as keys.</p>
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>On startup: {@link #load()} reads the state file and returns a map
 *       of device-id → previous state. The caller applies these to runtimes.</li>
 *   <li>On state change: {@link #setState(String, DeviceState)} updates the
 *       in-memory map and persists atomically.</li>
 * </ol>
 *
 * <h3>Corrupt file handling</h3>
 * A corrupt or unexpected file produces a warning log and an empty state map.
 */
public final class StateService {

    private static final Logger log = LoggerFactory.getLogger(StateService.class);
    private static final String STATE_FILE_VERSION = "1";

    private final Path stateFilePath;
    private final ConcurrentHashMap<String, DeviceState> stateMap;
    private final ObjectMapper mapper;

    /**
     * Creates a StateService backed by the given file.
     *
     * @param stateFilePath path to the JSON state file
     */
    public StateService(Path stateFilePath) {
        this.stateFilePath = stateFilePath;
        this.stateMap = new ConcurrentHashMap<>();
        this.mapper = new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Loads persisted state from disk.
     *
     * <p>If the file does not exist, returns an empty map.
     * If the file is corrupt or has an unexpected schema version,
     * logs a warning and returns an empty map.</p>
     *
     * @return map of device-id → state from the persisted file
     */
    public Map<String, DeviceState> load() {
        if (!Files.exists(stateFilePath)) {
            log.info("State file not found at {}; starting with empty state", stateFilePath);
            return Map.of();
        }

        try {
            String json = Files.readString(stateFilePath);
            VersionedState wrapper = mapper.readValue(json, VersionedState.class);

            if (!STATE_FILE_VERSION.equals(wrapper.version)) {
                log.warn("State file version {} does not match expected version {}; ignoring",
                        wrapper.version, STATE_FILE_VERSION);
                return Map.of();
            }

            Map<String, DeviceState> loaded = wrapper.map;
            log.info("Loaded {} device states from {}", loaded.size(), stateFilePath);

            // Seed in-memory map so setState/persist works correctly
            stateMap.clear();
            stateMap.putAll(loaded);

            return loaded;

        } catch (IOException e) {
            log.warn("Failed to load state file {}: {}; starting with empty state",
                    stateFilePath, e.getMessage());
            return Map.of();
        }
    }

    /**
     * Get the current state for a device.
     *
     * @param deviceId the stable device ID
     * @return the current state, or {@code null} if never set
     */
    public DeviceState getState(String deviceId) {
        return stateMap.get(deviceId);
    }

    /**
     * Update state for a device and persist atomically to disk.
     *
     * @param deviceId the stable device ID
     * @param state    the new state
     */
    public void setState(String deviceId, DeviceState state) {
        DeviceState previous = stateMap.put(deviceId, state);
        log.info("State change: device={} {} → {} (persisting)",
                deviceId, previous, state);
        persist();
    }

    /**
     * @return an unmodifiable view of all persisted states
     */
    public Map<String, DeviceState> getAllStates() {
        return Collections.unmodifiableMap(stateMap);
    }

    /**
     * Persist the current state map atomically to disk.
     */
    public void persist() {
        Path target = stateFilePath;
        Path tmp = Path.of(target.toString() + ".tmp");

        VersionedState wrapper = new VersionedState(STATE_FILE_VERSION, new HashMap<>(stateMap));
        String json;
        try {
            json = mapper.writeValueAsString(wrapper);
        } catch (IOException e) {
            log.error("Failed to serialize state: {}", e.getMessage());
            return;
        }

        try {
            Files.writeString(tmp, json);
            Files.move(tmp, target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            log.debug("Persisted {} states to {}", stateMap.size(), target);
        } catch (IOException e) {
            log.error("Failed to persist state to {}: {}", target, e.getMessage());
            // Clean up temp file on failure
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * JSON wrapper for versioned state persistence.
     */
    public static final class VersionedState {
        @JsonProperty("version")
        final String version;
        @JsonProperty("map")
        final Map<String, DeviceState> map;

        @JsonCreator
        public VersionedState(@JsonProperty("version") String version,
                              @JsonProperty("map") Map<String, DeviceState> map) {
            this.version = version;
            this.map = map;
        }
    }
}
