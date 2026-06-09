package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Root YAML config model — initial slice: node identity and stable IDs.
 * <p>
 * Schema version tracks config-file compatibility and is validated on load.
 * Currently only schema "1" is recognised; future schema versions must
 * fail fast rather than silently accepting unknown structures.
 */
public final class Sbc2haConfig {

    private static final Pattern STABLE_ID = Pattern.compile("^[a-z0-9][a-z0-9._-]*$");
    private static final String SUPPORTED_SCHEMA = "1";

    /**
     * Stable, machine-readable node identifier.
     * Must match {@code ^[a-z0-9][a-z0-9._-]*$}.
     */
    @JsonProperty("node_id")
    private String nodeId;

    /**
     * Config-file schema version (currently always {@code "1"}).
     */
    @JsonProperty("schema")
    private String schema;

    public Sbc2haConfig() {
    }

    public Sbc2haConfig(String nodeId, String schema) {
        this.nodeId = nodeId;
        this.schema = schema;
    }

    public String nodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String schema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Validate this configuration instance.
     *
     * @throws ValidationException if node_id is missing/invalid or schema is unknown
     */
    public void validate() {
        if (nodeId == null) {
            throw new ValidationException("node_id is required");
        }
        if (!STABLE_ID.matcher(nodeId).matches()) {
            throw new ValidationException(
                    "node_id must match ^[a-z0-9][a-z0-9._-]*$ but was: " + nodeId);
        }
        if (schema == null) {
            throw new ValidationException("schema is required");
        }
        if (!SUPPORTED_SCHEMA.equals(schema)) {
            throw new ValidationException(
                    "Unsupported schema version: " + schema + ". Supported: " + SUPPORTED_SCHEMA);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sbc2haConfig that = (Sbc2haConfig) o;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, schema);
    }

    @Override
    public String toString() {
        return "Sbc2haConfig{nodeId='" + nodeId + "', schema='" + schema + "'}";
    }
}
