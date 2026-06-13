package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DeviceMode;
import com.diozero.api.PinInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads pin-mode and chip-number overrides from resource files.
 *
 * <p>The old sbc2ha app used an independent {@code bbb-modes.txt} file to define
 * pin modes completely separate from diozero's board definitions.  This loader
 * reads that format and builds a lookup table so that
 * {@link PinInfoView} can inject the correct modes for each header-pin
 * pair, making the same code work on both kernel 4.x and 6.x BBBs.</p>
 *
 * <p>On kernel 6.x the gpiochip numbers and line offsets for BBB pins differ
 * from diozero's embedded board definition (written for kernel 4.x +
 * cape-universal).  This class loads {@code bbb-chip-mappings.txt} with the
 * full chip+line mapping and applies overrides automatically when running on
 * kernel >= 6.0, or when the system property
 * {@code sbc2ha.bbb.chip-override} is set to {@code true}.</p>
 *
 * <h3>Mode file format (bbb-modes.txt)</h3>
 * <pre>
 *   #HEADER,PIN,MODES
 *   P8,37,gpio_pu
 *   P8,39,gpio,gpio_pd
 *   P9,42,gpio,gpio_pd
 * </pre>
 * <p>Lines starting with {@code #} are comments.  Each data line has a header,
 * a pin number, and one or more mode strings ({@code gpio}, {@code gpio_pu},
 * {@code gpio_pd}).  Any line containing {@code gpio} implies the pin supports
 * digital input.</p>
 *
 * <h3>Chip + line mapping format (bbb-chip-mappings.txt)</h3>
 * <pre>
 *   #HEADER,PIN,DIOZERO_CHIP,DIOZERO_LINE,KERNEL_CHIP,KERNEL_LINE
 *   P8,37,2,14,1,14
 *   P9,15,1,16,1,0
 * </pre>
 * <p>Lines starting with {@code #} are comments.  Each data line specifies the
 * diozero chip/line and the corresponding kernel 6.x chip/line for a pin.
 * If KERNEL_CHIP differs from DIOZERO_CHIP, the chip is overridden.
 * If KERNEL_LINE differs from DIOZERO_LINE, the line offset is overridden.</p>
 */
final class PinModeOverrides {

    private static final Logger log = LoggerFactory.getLogger(PinModeOverrides.class);

    /** System property to force chip overrides on (default: auto-detect kernel version). */
    private static final String CHIP_OVERRIDE_PROP = "sbc2ha.bbb.chip-override";

    /** Minimum kernel version for chip overrides (kernel 6.0). */
    private static final int MIN_KERNEL_MAJOR = 6;

    /** Number of columns in the chip+line mapping file. */
    private static final int FULL_FORMAT_COLUMNS = 6;
    /** Minimum columns for backward-compatible chip-only format. */
    private static final int MIN_COLUMNS = 3;

    private PinModeOverrides() {
        // utility class
    }

    /**
     * Key into the override map: {@code header + ":" + pinNumber}.
     */
    static String key(String header, int pin) {
        return header + ":" + pin;
    }

    /**
     * Immutable holder for chip and line offset overrides for a single pin.
     */
    record PinOverride(Integer chip, Integer lineOffset) {
        static PinOverride of(Integer chip, Integer lineOffset) {
            return new PinOverride(chip, lineOffset);
        }
    }

    /**
     * Build a map from a pin's identifying info to override modes.
     *
     * @param pinInfo the pin resolved from diozero board definitions
     * @return a map of {@code header:pin → overrideModes}, never null
     */
    static Map<String, Set<DeviceMode>> forPin(PinInfo pinInfo) {
        Map<String, Set<DeviceMode>> m = new HashMap<>();
        String header = pinInfo.getHeader();
        if (header != null && !PinInfo.DEFAULT_HEADER.equals(header)) {
            m.put(key(header, pinInfo.getPhysicalPin()),
                  Collections.singleton(DeviceMode.DIGITAL_INPUT));
        }
        return m;
    }

    // ── Mode overrides ──────────────────────────────────────────────────

    /**
     * Load all mode overrides from the classpath resource {@code bbb-modes.txt}.
     *
     * @param resourcePath path to the resource; empty string means "load from
     *                     classpath", non-empty treated as a resource path
     * @return combined override map for every entry in the file
     */
    static Map<String, Set<DeviceMode>> loadModes(String resourcePath) {
        String path = resourcePath.isEmpty()
                ? "hardware-profiles/boneio/bbb-modes.txt"
                : resourcePath;

        Map<String, Set<DeviceMode>> combined = new HashMap<>();

        try (InputStream is = PinModeOverrides.class.getClassLoader()
                     .getResourceAsStream(path)) {
            if (is == null) {
                log.warn("No override resource found at '{}' — all pins will use diozero board defs", path);
                return combined;
            }
            try (var reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.lines()
                      .filter(line -> !line.isEmpty() && !line.trim().startsWith("#"))
                      .map(String::trim)
                      .forEach(line -> parseAndMerge(line, combined));
            }
            log.info("Loaded {} pin-mode overrides from '{}'",
                     combined.size(), path);
        } catch (IOException e) {
            log.warn("Failed to read override resource '{}': {}", path, e.getMessage());
        }
        return combined;
    }

    // ── Chip + line offset overrides ────────────────────────────────────

    /**
     * Load chip number and line offset overrides from {@code bbb-chip-mappings.txt}
     * if the override is enabled (kernel >= 6.0 or system property set).
     *
     * <p>Parses both the full 6-column format
     * ({@code HEADER,PIN,DIOZERO_CHIP,DIOZERO_LINE,KERNEL_CHIP,KERNEL_LINE})
     * and the legacy 3-column format ({@code HEADER,PIN,CHIP_ID}) for
     * backward compatibility.</p>
     *
     * @param resourcePath path to the resource; empty string means "load from classpath"
     * @return map of {@code header:pin → PinOverride}, empty when overrides are disabled
     */
    static Map<String, PinOverride> loadChipOverrides(String resourcePath) {
        if (!isChipOverrideEnabled()) {
            log.debug("BBB chip overrides disabled (kernel < {} or system property not set)", MIN_KERNEL_MAJOR);
            return Map.of();
        }

        String path = resourcePath.isEmpty()
                ? "hardware-profiles/boneio/bbb-chip-mappings.txt"
                : resourcePath;

        Map<String, PinOverride> overrides = new HashMap<>();

        try (InputStream is = PinModeOverrides.class.getClassLoader()
                     .getResourceAsStream(path)) {
            if (is == null) {
                log.warn("No chip-mapping resource found at '{}' — chip overrides disabled", path);
                return overrides;
            }
            try (var reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.lines()
                      .filter(line -> !line.isEmpty() && !line.trim().startsWith("#"))
                      .map(String::trim)
                      .forEach(line -> parseChipMapping(line, overrides));
            }
            log.info("Loaded {} BBB chip+line overrides from '{}' (kernel >= {})",
                     overrides.size(), path, MIN_KERNEL_MAJOR);
        } catch (IOException e) {
            log.warn("Failed to read chip-mapping resource '{}': {}", path, e.getMessage());
        }
        return overrides;
    }

    /**
     * Check whether chip overrides should be applied.
     *
     * <p>Overrides are enabled when:</p>
     * <ul>
     *   <li>System property {@code sbc2ha.bbb.chip-override} is {@code "true"}, OR</li>
     *   <li>Kernel version is >= 6.0 (auto-detected from {@code os.version})</li>
     * </ul>
     *
     * <p>On failure to detect the kernel version, overrides are <em>disabled</em>
     * for safety (fail-open: use diozero defaults).</p>
     */
    static boolean isChipOverrideEnabled() {
        // Explicit system property takes priority
        String prop = System.getProperty(CHIP_OVERRIDE_PROP);
        if ("true".equals(prop)) {
            log.info("BBB chip overrides enabled via system property '{}'", CHIP_OVERRIDE_PROP);
            return true;
        }
        if ("false".equals(prop)) {
            return false;
        }

        // Auto-detect: read os.version system property (set by diozero's LocalSystemInfo)
        String kernelVersion = System.getProperty("os.version", "");
        return detectKernelVersion(kernelVersion) >= MIN_KERNEL_MAJOR;
    }

    /**
     * Parse kernel version from a version string like "6.6.47-ti-r70".
     *
     * @return major version number, or -1 if parsing fails
     */
    static int detectKernelVersion(String versionString) {
        if (versionString == null || versionString.isBlank()) {
            return -1;
        }
        try {
            // Extract leading digits
            int dot = versionString.indexOf('.');
            if (dot < 0) {
                return Integer.parseInt(versionString.trim());
            }
            return Integer.parseInt(versionString.substring(0, dot).trim());
        } catch (NumberFormatException e) {
            log.debug("Could not parse kernel version from '{}': {}", versionString, e.getMessage());
            return -1;
        }
    }

    private static void parseChipMapping(String line, Map<String, PinOverride> overrides) {
        // Format: HEADER,PIN[,DIOZERO_CHIP,DIOZERO_LINE,]KERNEL_CHIP[,KERNEL_LINE]
        // Full:     HEADER,PIN,DIOZERO_CHIP,DIOZERO_LINE,KERNEL_CHIP,KERNEL_LINE
        // Legacy:   HEADER,PIN,CHIP_ID
        java.util.List<String> cols = java.util.Arrays.stream(line.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        if (cols.size() < MIN_COLUMNS) return;

        String header = cols.get(0);
        int pin;
        try {
            pin = Integer.parseInt(cols.get(1));
        } catch (NumberFormatException e) {
            log.debug("Skipping malformed chip-mapping line (bad pin): {}", line);
            return;
        }

        if (cols.size() == FULL_FORMAT_COLUMNS) {
            // Full 6-column format: HEADER,PIN,DIOZERO_CHIP,DIOZERO_LINE,KERNEL_CHIP,KERNEL_LINE
            int dioChip, dioLine, kernChip, kernLine;
            try {
                dioChip = Integer.parseInt(cols.get(2));
                dioLine = Integer.parseInt(cols.get(3));
                kernChip = Integer.parseInt(cols.get(4));
                kernLine = Integer.parseInt(cols.get(5));
            } catch (NumberFormatException e) {
                log.debug("Skipping malformed chip-mapping line (bad numbers): {}", line);
                return;
            }

            // Only include if chip or line differs
            if (kernChip != dioChip || kernLine != dioLine) {
                overrides.put(key(header, pin),
                        PinOverride.of(kernChip, kernLine));
            }
        } else if (cols.size() == MIN_COLUMNS) {
            // Legacy 3-column format: HEADER,PIN,CHIP_ID
            int chipId;
            try {
                chipId = Integer.parseInt(cols.get(2));
            } catch (NumberFormatException e) {
                log.debug("Skipping malformed chip-mapping line (bad chip): {}", line);
                return;
            }
            overrides.put(key(header, pin),
                    PinOverride.of(chipId, null));
        }
    }

    // ── Mode merge (unchanged) ──────────────────────────────────────────

    private static void parseAndMerge(String line, Map<String, Set<DeviceMode>> combined) {
        // Split on comma: HEADER,PIN[,MODE1,MODE2,...]
        int firstComma = line.indexOf(',');
        if (firstComma < 0) return;
        String header = line.substring(0, firstComma).trim();

        int secondComma = line.indexOf(',', firstComma + 1);
        if (secondComma < 0) {
            // HEADER,PIN only — treat as gpio
            try {
                int pin = Integer.parseInt(line.substring(firstComma + 1).trim());
                Set<String> modes = Collections.singleton("gpio");
                mergeHeader(combined, header, pin, modes);
            } catch (NumberFormatException e) {
                log.debug("Skipping malformed override line: {}", line);
            }
            return;
        }

        try {
            int pin = Integer.parseInt(line.substring(firstComma + 1, secondComma).trim());
            String modeStr = line.substring(secondComma + 1).trim();
            Set<String> modes = java.util.Arrays.stream(modeStr.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            mergeHeader(combined, header, pin, modes);
        } catch (NumberFormatException e) {
            log.debug("Skipping malformed override line: {}", line);
        }
    }

    private static void mergeHeader(Map<String, Set<DeviceMode>> combined,
                                     String header, int pin, Set<String> modes) {
        // Any "gpio*" mode (gpio, gpio_pu, gpio_pd) implies DIGITAL_INPUT
        boolean hasGpio = modes.stream().anyMatch(m -> m.startsWith("gpio"));
        if (!hasGpio) return;
        String k = key(header, pin);
        Set<DeviceMode> existing = combined.computeIfAbsent(k, __ -> java.util.EnumSet.noneOf(DeviceMode.class));
        existing.add(DeviceMode.DIGITAL_INPUT);
    }

    // ── Wrapping ────────────────────────────────────────────────────────

    /**
     * Given a resolved pin's override map, produce the {@link PinInfoView}.
     *
     * @param pinInfo     the original pin (may have empty modes)
     * @param pinOverrides map for this specific pin
     * @return a PinInfoView that includes override modes, or the original
     *         pinInfo if no overrides apply
     */
    static PinInfo wrap(PinInfo pinInfo, Map<String, Set<DeviceMode>> pinOverrides) {
        String k = key(pinInfo.getHeader(), pinInfo.getPhysicalPin());
        Set<DeviceMode> overrideModes = pinOverrides.get(k);
        if (overrideModes == null || overrideModes.isEmpty()) {
            return new PinInfoView(pinInfo, null, null, null);
        }
        return new PinInfoView(pinInfo, overrideModes, null, null);
    }

    /**
     * Wrap a pin with both mode overrides and chip/line offset overrides.
     *
     * @param pinInfo        the original pin
     * @param modeOverrides  map of mode overrides, may be null
     * @param chipLineOverrides global chip/line override map (header:pin → PinOverride)
     * @return a PinInfoView with combined overrides, or the original pinInfo
     */
    static PinInfoView wrap(PinInfo pinInfo, Map<String, Set<DeviceMode>> modeOverrides,
                            Map<String, PinOverride> chipLineOverrides) {
        String k = key(pinInfo.getHeader(), pinInfo.getPhysicalPin());
        Set<DeviceMode> overrideModes = modeOverrides != null ? modeOverrides.get(k) : null;
        PinOverride pinOverride = chipLineOverrides != null ? chipLineOverrides.get(k) : null;

        Integer overrideChip = pinOverride != null ? pinOverride.chip() : null;
        Integer overrideLine = pinOverride != null ? pinOverride.lineOffset() : null;

        // No overrides → return the original pinInfo directly (zero overhead)
        if ((overrideModes == null || overrideModes.isEmpty()) && overrideChip == null
                && overrideLine == null) {
            return new PinInfoView(pinInfo, null, null, null);
        }
        return new PinInfoView(pinInfo, overrideModes, overrideChip, overrideLine);
    }
}
