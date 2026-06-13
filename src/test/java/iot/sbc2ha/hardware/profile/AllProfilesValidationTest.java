package iot.sbc2ha.hardware.profile;

import iot.sbc2ha.hardware.GpioChannel;
import iot.sbc2ha.hardware.HardwareMapping;
import iot.sbc2ha.hardware.PhysicalChannel;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Validate every YAML profile under src/main/resources/hardware-profiles/.
 * <p>
 * Each profile is tested for:
 * <ol>
 *   <li>Valid YAML syntax (Jackson can parse it)</li>
 *   <li>{@code incomplete == false}</li>
 *   <li>Channel count == mapping count</li>
 *   <li>No duplicate channel locations</li>
 *   <li>No duplicate mapping logical_ids</li>
 * </ol>
 * <p>
 * This catches the kind of errors that went undetected in review:
 * - ADC channels in BBB nohat were only in mappings, not channels
 * - RPI nohat had duplicate GPIO_23/24/26 at 2 physical pins each
 * - Several profiles had {@code incomplete: true} without reason
 */
class AllProfilesValidationTest {

    @TestFactory
    Stream<DynamicTest> allProfilesShouldBeValid() throws Exception {
        URL profilesUrl = getClass().getClassLoader().getResource("hardware-profiles");
        assertNotNull(profilesUrl, "hardware-profiles/ not found on classpath");

        List<String> yamlPaths = discoverYamlFiles(profilesUrl);
        assertFalse(yamlPaths.isEmpty(), "No .yaml files found under hardware-profiles/");

        return yamlPaths.stream()
                .flatMap(this::validateProfile);
    }

    private List<String> discoverYamlFiles(URL rootUrl) throws Exception {
        Path root = Path.of(rootUrl.toURI());

        // If root is not a directory (e.g. inside JAR), fall back to walking main resources
        if (!Files.isDirectory(root)) {
            // Try filesystem path from project root
            Path fallback = Path.of("src/main/resources/hardware-profiles");
            if (Files.isDirectory(fallback)) {
                return scanDirectory(fallback);
            }
            throw new RuntimeException("hardware-profiles/ not found on filesystem either");
        }

        return scanDirectory(root);
    }

    private List<String> scanDirectory(Path root) throws Exception {
        List<String> result = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".yaml")) {
                    String parentPath = root.getParent().toString();
                    String path = file.toString().replace(parentPath + File.separatorChar, "");
                    path = path.replace(File.separatorChar, '/');
                    result.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        result.sort(null);
        return result;
    }

    private Stream<DynamicTest> validateProfile(String relativePath) {
        String name = relativePath.replace("hardware-profiles/", "")
                .replace('/', '-').replace(".yaml", "");

        HardwareProfile profile = loadProfile(relativePath);

        if (profile == null) {
            // YAML failed to parse — just report the error once
            return Stream.of(
                    dynamicTest(name + " — YAML parses",
                            () -> fail(relativePath + ": ProfileLoadingException (see console)")));
        }

        return Stream.of(
                dynamicTest(name + " — incomplete is false",
                        () -> assertFalse(profile.incomplete(),
                                relativePath + ": incomplete=true — channels/mappings are out of sync")),

                dynamicTest(name + " — channel count == mapping count",
                        () -> assertEquals(profile.channelCount(), profile.mappingCount(),
                                relativePath + ": " + profile.channelCount() + " channels vs "
                                        + profile.mappingCount() + " mappings")),

                dynamicTest(name + " — no duplicate channel definitions",
                        () -> {
                            Set<String> duplicates = profile.channels().stream()
                                    .map(this::channelKey)
                                    .collect(Collectors.groupingBy(key -> key, Collectors.counting()))
                                    .entrySet().stream()
                                    .filter(e -> e.getValue() > 1)
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toSet());
                            assertTrue(duplicates.isEmpty(),
                                    relativePath + ": duplicate channel definitions: " + duplicates);
                        }),

                dynamicTest(name + " — no duplicate mapping logical_ids",
                        () -> {
                            Set<String> duplicates = profile.mappings().stream()
                                    .map(HardwareMapping::logicalId)
                                    .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
                                    .entrySet().stream()
                                    .filter(e -> e.getValue() > 1)
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toSet());
                            assertTrue(duplicates.isEmpty(),
                                    relativePath + ": duplicate logical_ids: " + duplicates);
                        })
        );
    }

    private HardwareProfile loadProfile(String relativePath) {
        try {
            ProfileRegistry registry = new ProfileRegistry();
            return registry.loadFromClasspath(relativePath.replace("\\", "/"));
        } catch (ProfileLoadingException e) {
            System.err.println("LOAD ERROR [" + relativePath + "]: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  Cause: " + e.getCause().getMessage());
            }
            return null;
        }
    }

    /**
     * Build a unique key for a channel to detect duplicates.
     * <p>
     * GPIO channels use just their pin name (e.g. "P9_11").
     * I2C channels (MCP23017, PCA9685, OLED) use bus:pin (e.g. "mcp1:0").
     * OLED channels have a bus but no pin — key is just the bus.
     */
    private String channelKey(PhysicalChannel ch) {
        if (ch.channelType() == PhysicalChannel.ChannelType.GPIO) {
            return ((GpioChannel) ch).pinLabel();
        }
        // For I2C channels, pin() is the hardware pin number
        // For OLED, pin is -1 — key is just the bus
        int pin = ch.pin();
        if (pin >= 0) {
            return ch.bus() + ":" + pin;
        }
        // OLED: bus only
        return ch.bus();
    }
}
