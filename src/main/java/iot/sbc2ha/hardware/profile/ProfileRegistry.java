package iot.sbc2ha.hardware.profile;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import iot.sbc2ha.hardware.GpioChannel;
import iot.sbc2ha.hardware.HardwareChip;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.PhysicalChannel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Registry of hardware profiles with expansion, override, and loading support.
 * <p>
 * Profiles are registered manually or loaded from classpath resources / files.
 * {@link #expand(String, List, List)} merges a profile with optional overrides
 * to produce a {@link HardwareModel}.
 * <p>
 * Referencing an unknown profile name throws {@link ProfileLoadingException}.
 *
 * <h3>Override semantics</h3>
 * <ul>
 *   <li>Override channels replace profile channels by matching location.</li>
 *   <li>Override mappings replace profile mappings by matching logical_id.</li>
 * </ul>
 */
public final class ProfileRegistry {

    private final Map<String, HardwareProfile> profiles = new LinkedHashMap<>();
    private final JsonMapper yamlMapper;

    public ProfileRegistry() {
        this.yamlMapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    /**
     * Register a profile manually.
     *
     * @param profile the profile to register
     * @throws ProfileLoadingException if a profile with the same ID is already registered
     */
    public void register(HardwareProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");
        String id = profile.id();
        if (id == null) {
            throw new ProfileLoadingException("Profile must have a non-null id");
        }
        // Idempotent: skip if already registered (loadFromClasspath may have already registered)
        if (profiles.containsKey(id)) {
            return;
        }
        profiles.put(id, profile);
    }

    /**
     * Load a profile from a classpath resource (YAML).
     *
     * @param resourcePath classpath resource path (e.g. "hardware-profiles/boneio/input-v0.3.yaml")
     * @return the loaded profile
     * @throws ProfileLoadingException if the resource is not found or cannot be parsed
     */
    public HardwareProfile loadFromClasspath(String resourcePath) {
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new ProfileLoadingException("Profile resource not found on classpath: " + resourcePath);
        }
        HardwareProfile profile = loadFromUrl(url);
        register(profile);
        return profile;
    }

    /**
     * Load a profile from a file path.
     *
     * @param path path to a YAML file
     * @return the loaded profile
     * @throws ProfileLoadingException if the file is not found or cannot be parsed
     */
    public HardwareProfile loadFromFile(Path path) {
        try {
            HardwareProfile profile = yamlMapper.readValue(path.toFile(), HardwareProfile.class);
            register(profile);
            return profile;
        } catch (IOException e) {
            throw new ProfileLoadingException("Failed to load profile from file: " + path, e);
        }
    }

    private HardwareProfile loadFromUrl(URL url) {
        try (InputStream is = url.openStream()) {
            return yamlMapper.readValue(is, HardwareProfile.class);
        } catch (IOException e) {
            throw new ProfileLoadingException("Failed to load profile from URL: " + url, e);
        }
    }

    /**
     * Expand a registered profile into a HardwareModel.
     * <p>
     * The resulting model uses the profile's chips, channels and mappings directly.
     * Additional channels and mappings passed as arguments are appended after
     * the profile's contents (useful for extending a profile).
     *
     * @param profileName       the registered profile name
     * @param extraChannels     optional additional channels to append
     * @param extraMappings     optional additional mappings to append
     * @return a HardwareModel containing profile + extras
     * @throws ProfileLoadingException if the profile name is not found
     */
    public HardwareModel expand(String profileName,
                                List<PhysicalChannel> extraChannels,
                                List<HardwareMapping> extraMappings) {
        HardwareProfile profile = profiles.get(profileName);
        if (profile == null) {
            throw new ProfileLoadingException("Unknown profile: " + profileName
                    + ". Registered profiles: " + profiles.keySet());
        }

        List<HardwareChip> allChips = profile.chips() != null
                ? new ArrayList<>(profile.chips()) : new ArrayList<>();
        List<PhysicalChannel> allChannels = new ArrayList<>(profile.channels());
        List<HardwareMapping> allMappings = new ArrayList<>(profile.mappings());

        if (extraChannels != null) {
            allChannels.addAll(extraChannels);
        }
        if (extraMappings != null) {
            allMappings.addAll(extraMappings);
        }

        return new HardwareModel(profileName, profileName, allChips, allChannels, allMappings);
    }

    /**
     * Expand a profile with overrides — channels and mappings replace
     * profile entries with the same identifier.
     * <p>
     * Override channels replace by bus id; override mappings replace by logical_id.
     *
     * @param profileName      the registered profile name
     * @param overrideChannels channels that replace matching profile channels
     * @param overrideMappings mappings that replace matching profile mappings
     * @return a HardwareModel with overrides applied
     * @throws ProfileLoadingException if the profile name is not found
     */
    public HardwareModel expandWithOverrides(String profileName,
                                             List<PhysicalChannel> overrideChannels,
                                             List<HardwareMapping> overrideMappings) {
        HardwareProfile profile = profiles.get(profileName);
        if (profile == null) {
            throw new ProfileLoadingException("Unknown profile: " + profileName
                    + ". Registered profiles: " + profiles.keySet());
        }

        List<HardwareChip> allChips = profile.chips() != null
                ? new ArrayList<>(profile.chips()) : new ArrayList<>();
        List<PhysicalChannel> channels = applyChannelOverrides(profile.channels(), overrideChannels);
        List<HardwareMapping> mappings = applyMappingOverrides(profile.mappings(), overrideMappings);

        return new HardwareModel(profileName, profileName, allChips, channels, mappings);
    }

    private static List<PhysicalChannel> applyChannelOverrides(
            List<PhysicalChannel> profileChannels,
            List<PhysicalChannel> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return profileChannels;
        }

        // Build a map of channel identifier → override channel.
        // I2C channels (MCP23017, PCA9685, OLED) use bus id.
        // GPIO channels use pin label.
        Map<String, PhysicalChannel> overrideMap = new LinkedHashMap<>();
        for (PhysicalChannel ch : overrides) {
            overrideMap.put(channelKey(ch), ch);
        }

        List<PhysicalChannel> result = new ArrayList<>();
        for (PhysicalChannel ch : profileChannels) {
            String key = channelKey(ch);
            PhysicalChannel override = overrideMap.get(key);
            result.add(Objects.requireNonNullElse(override, ch));
        }
        // Append overrides that don't match any profile channel
        for (Map.Entry<String, PhysicalChannel> entry : overrideMap.entrySet()) {
            boolean found = profileChannels.stream()
                    .anyMatch(ch -> channelKey(ch).equals(entry.getKey()));
            if (!found) {
                result.add(entry.getValue());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Build a unique key for a channel for override matching.
     * I2C channels (bus != null) use bus id.
     * GPIO channels use pin label.
     */
    private static String channelKey(PhysicalChannel ch) {
        if (ch.bus() != null) {
            return ch.bus();
        }
        if (ch instanceof GpioChannel gpio) {
            return gpio.pinLabel();
        }
        return ch.toString();
    }

    private static List<HardwareMapping> applyMappingOverrides(
            List<HardwareMapping> profileMappings,
            List<HardwareMapping> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return profileMappings;
        }

        Map<String, HardwareMapping> overrideMap = new LinkedHashMap<>();
        for (HardwareMapping m : overrides) {
            overrideMap.put(m.logicalId(), m);
        }

        List<HardwareMapping> result = new ArrayList<>();
        for (HardwareMapping m : profileMappings) {
            HardwareMapping override = overrideMap.get(m.logicalId());
            result.add(Objects.requireNonNullElse(override, m));
        }
        for (Map.Entry<String, HardwareMapping> entry : overrideMap.entrySet()) {
            if (profileMappings.stream().noneMatch(m -> m.logicalId().equals(entry.getKey()))) {
                result.add(entry.getValue());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Check if a profile is registered.
     */
    public boolean hasProfile(String name) {
        return profiles.containsKey(name);
    }

    /**
     * Number of registered profiles.
     */
    public int profileCount() {
        return profiles.size();
    }

    /**
     * Names of all registered profiles.
     */
    public List<String> registeredProfileNames() {
        return Collections.unmodifiableList(new ArrayList<>(profiles.keySet()));
    }

    @Override
    public String toString() {
        return "ProfileRegistry{profiles=" + profiles.keySet() + "}";
    }
}
