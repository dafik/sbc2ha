package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import iot.sbc2ha.device.DeviceRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Root YAML config model.
 * <p>
 * Supports two formats:
 * <ul>
 *   <li>Legacy (pre-SBC-009): root-level {@code node_id}, {@code schema}, {@code devices}</li>
 *   <li>Profile-based (SBC-009+): {@code sbc2ha: { id, schema }}, {@code runtime}, {@code hardware}, {@code locations}, {@code devices}, {@code mqtt}, {@code home_assistant}</li>
 * </ul>
 * <p>
 * Schema version tracks config-file compatibility and is validated on load.
 * Currently only schema "1" is recognised; future schema versions must
 * fail fast rather than silently accepting unknown structures.
 */
public final class Sbc2haConfig {

    private static final Pattern STABLE_ID = Pattern.compile("^[a-z0-9][a-z0-9._-]*$");
    private static final String SUPPORTED_SCHEMA = "1";

    /**
     * Legacy root-level node identifier (pre-SBC-009).
     */
    @JsonProperty("node_id")
    private String nodeId;

    /**
     * Legacy root-level schema version (pre-SBC-009).
     */
    @JsonProperty("schema")
    private String schema;

    /**
     * Profile-based wrapper — SBC-009+ format.
     * Contains {@code id} and optionally {@code schema}.
     */
    @JsonProperty("sbc2ha")
    private Sbc2haNodeConfig sbc2ha;

    /**
     * Runtime configuration.
     */
    @JsonProperty("runtime")
    private RuntimeConfig runtime;

    /**
     * Hardware configuration.
     */
    @JsonProperty("hardware")
    private HardwareConfig hardware;

    /**
     * Location definitions — maps location path to metadata.
     */
    @JsonProperty("locations")
    private Map<String, LocationConfig> locations = new HashMap<>();

    /**
     * List of devices (polymorphic: {@code type} field determines actual subtype).
     */
    @JsonProperty("devices")
    private List<iot.sbc2ha.device.DeviceConfig> devices = new ArrayList<>();

    /**
     * MQTT configuration.
     */
    @JsonProperty("mqtt")
    private MqttConfig mqtt;

    /**
     * Home Assistant configuration.
     */
    @JsonProperty("home_assistant")
    private HomeAssistantConfig homeAssistant;

    /**
     * Validated device registry — populated after {@link #validate()}.
     */
    private transient DeviceRegistry registry;

    public Sbc2haConfig() {
    }

    public Sbc2haConfig(String nodeId, String schema) {
        this.nodeId = nodeId;
        this.schema = schema;
    }

    /**
     * @return stable, machine-readable node identifier
     */
    public String nodeId() {
        if (sbc2ha != null && sbc2ha.id() != null) {
            return sbc2ha.id();
        }
        return nodeId;
    }

    /**
     * Setter for legacy root-level nodeId (for programmatic test use).
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * @return config-file schema version
     */
    public String schema() {
        if (sbc2ha != null && sbc2ha.schema() != null) {
            return sbc2ha.schema();
        }
        return schema;
    }

    /**
     * Setter for legacy root-level schema (for programmatic test use).
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @SuppressWarnings("unused")
    public RuntimeConfig runtime() {
        return runtime;
    }

    public void setRuntime(RuntimeConfig runtime) {
        this.runtime = runtime;
    }

    @SuppressWarnings("unused")
    public HardwareConfig hardware() {
        return hardware;
    }

    public void setHardware(HardwareConfig hardware) {
        this.hardware = hardware;
    }

    public Map<String, LocationConfig> locations() {
        return locations;
    }

    @SuppressWarnings("unused")
    public void setLocations(Map<String, LocationConfig> locations) {
        this.locations = locations;
    }

    public List<iot.sbc2ha.device.DeviceConfig> devices() {
        return devices;
    }

    public void setDevices(List<iot.sbc2ha.device.DeviceConfig> devices) {
        this.devices = devices;
    }

    public MqttConfig mqtt() {
        return mqtt;
    }

    public void setMqtt(MqttConfig mqtt) {
        this.mqtt = mqtt;
    }

    public HomeAssistantConfig homeAssistant() {
        return homeAssistant;
    }

    @SuppressWarnings("unused")
    public void setHomeAssistant(HomeAssistantConfig homeAssistant) {
        this.homeAssistant = homeAssistant;
    }

    /**
     * @return the validated device registry, or {@code null} if {@link #validate()} has not been called
     */
    public DeviceRegistry registry() {
        return registry;
    }

    /**
     * Validate this configuration instance.
     *
     * <p>Also builds and caches the {@link DeviceRegistry} for later
     * runtime use.</p>
     *
     * @throws ValidationException if node_id is missing/invalid, schema is unknown, or device validation fails
     */
    public void validate() {
        String effectiveNodeId = nodeId();
        String effectiveSchema = schema();

        if (effectiveNodeId == null) {
            throw new ValidationException("node_id is required");
        }
        if (!STABLE_ID.matcher(effectiveNodeId).matches()) {
            throw new ValidationException(
                    "node_id must match ^[a-z0-9][a-z0-9._-]*$ but was: " + effectiveNodeId);
        }
        if (effectiveSchema == null) {
            throw new ValidationException("schema or sbc2ha.schema is required");
        }
        if (!SUPPORTED_SCHEMA.equals(effectiveSchema)) {
            throw new ValidationException(
                    "Unsupported schema version: " + effectiveSchema + ". Supported: " + SUPPORTED_SCHEMA);
        }
        // Build and cache the validated device registry
        DeviceRegistry reg = new DeviceRegistry();
        for (iot.sbc2ha.device.DeviceConfig dev : devices) {
            reg.add(dev);
        }
        reg.validate();
        this.registry = reg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sbc2haConfig that = (Sbc2haConfig) o;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(schema, that.schema)
                && Objects.equals(sbc2ha, that.sbc2ha)
                && Objects.equals(runtime, that.runtime)
                && Objects.equals(hardware, that.hardware)
                && Objects.equals(locations, that.locations)
                && Objects.equals(devices, that.devices)
                && Objects.equals(mqtt, that.mqtt)
                && Objects.equals(homeAssistant, that.homeAssistant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, schema, sbc2ha, runtime, hardware, locations, devices, mqtt, homeAssistant);
    }

    @Override
    public String toString() {
        return "Sbc2haConfig{nodeId='" + nodeId() + "', schema='" + schema() + "', devices=" + devices.size() + "}";
    }

    /**
     * Profile-based wrapper for sbc2ha node identity — nested under {@code sbc2ha:}.
     */
    public static final class Sbc2haNodeConfig {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("schema")
        private String schema;

        public Sbc2haNodeConfig() {
        }

        public String id() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String schema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }
}
