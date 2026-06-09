package iot.sbc2ha.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads {@link Sbc2haConfig} from a YAML file on the filesystem.
 * <p>
 * The loader expects a plain file path (config is instance-specific
 * and never bundled inside a JAR).
 * <p>
 * After deserialisation the returned config is validated; on failure
 * a {@link ValidationException} is thrown.
 */
public final class ConfigLoader {

    private static final JsonMapper MAPPER = JsonMapper.builder(new YAMLFactory())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .addModule(new JavaTimeModule())
            .build();

    /**
     * Load and validate a {@link Sbc2haConfig} from the given path.
     *
     * @param path path to a YAML config file
     * @return validated configuration
     * @throws ValidationException if the config is missing, malformed, or fails validation
     */
    public static Sbc2haConfig load(Path path) {
        if (path == null || !Files.exists(path) || !Files.isReadable(path)) {
            throw new ValidationException("Config file not found or not readable: " + path);
        }
        try {
            Sbc2haConfig config = MAPPER.readValue(
                    path.toFile(),
                    TypeFactory.defaultInstance().constructType(Sbc2haConfig.class)
            );
            config.validate();
            return config;
        } catch (IOException e) {
            throw new ValidationException("Failed to load config from " + path, e);
        }
    }

    /**
     * Convenience: load from a string path.
     */
    public static Sbc2haConfig load(String path) {
        return load(Path.of(path));
    }
}
