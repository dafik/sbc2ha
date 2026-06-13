package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DeviceMode;
import com.diozero.api.PinInfo;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PinModeOverrides} loading and pin wrapping.
 */
class PinModeOverridesTest {

    @Test
    void loadModes_from_classpath_resource() {
        // The resource is at hardware-profiles/boneio/bbb-modes.txt
        Map<String, java.util.Set<DeviceMode>> overrides = PinModeOverrides.loadModes("");
        // bbb-modes.txt has entries, all contain "gpio" (with or without pull config)
        assertFalse(overrides.isEmpty(), "Should load at least some overrides from classpath");
        System.out.println("Loaded keys: " + overrides.keySet());
        System.out.println("Total: " + overrides.size());
        // Verify a known BBB pin
        assertTrue(overrides.containsKey("P8:37"), "Should have P8_37 entry");
        assertTrue(overrides.containsKey("P9:42"), "Should have P9_42 entry");
        // P8_37 should support DIGITAL_INPUT
        java.util.Set<DeviceMode> p8_37 = overrides.get("P8:37");
        assertNotNull(p8_37, "P8:37 override set should not be null");
        assertTrue(p8_37.contains(DeviceMode.DIGITAL_INPUT),
                  "P8_37 should support DIGITAL_INPUT via gpio_pu mode");
    }

    @Test
    void forPin_builds_correct_key() {
        // DEFAULT header → empty result (filtered out)
        PinInfo pinInfo = new PinInfo("test", PinInfo.DEFAULT_HEADER, 37, 37, "GPIO0",
                java.util.Collections.emptyList());
        Map<String, java.util.Set<DeviceMode>> result = PinModeOverrides.forPin(pinInfo);
        assertTrue(result.isEmpty(), "DEFAULT header should produce empty overrides");

        pinInfo = new PinInfo("test", "P8", 37, 37, "GPIO0",
                java.util.Collections.emptyList());
        result = PinModeOverrides.forPin(pinInfo);
        assertTrue(result.containsKey("P8:37"), "Should create key for non-DEFAULT header");
    }

    @Test
    void wrap_with_no_overrides_returns_view_with_empty_override() {
        PinInfo pinInfo = new PinInfo("test", "P8", 37, 37, "GPIO0",
                java.util.Collections.singleton(DeviceMode.DIGITAL_INPUT));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, Map.of(), Map.of());
        assertNotNull(view);
        assertSame(pinInfo, view.delegate());
        // Original pin supports DIGITAL_INPUT, view should too
        assertTrue(view.isSupported(DeviceMode.DIGITAL_INPUT));
    }

    @Test
    void wrap_injects_override_modes() {
        // Pin with empty modes (like LCD cape pins)
        PinInfo pinInfo = new PinInfo("test", "P8", 37, 37, "LCD_DATA8",
                java.util.Collections.emptyList());

        Map<String, java.util.Set<DeviceMode>> modeOverrides = Map.of(
                "P8:37", java.util.Set.of(DeviceMode.DIGITAL_INPUT));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, modeOverrides, Map.of());
        assertNotNull(view);
        // Original pin does NOT support DIGITAL_INPUT (empty modes)
        assertFalse(pinInfo.isSupported(DeviceMode.DIGITAL_INPUT));
        // But the view should, via override
        assertTrue(view.isSupported(DeviceMode.DIGITAL_INPUT));
    }

    @Test
    void wrap_preserves_original_attributes() {
        PinInfo pinInfo = new PinInfo("kp", "P9", 42, 42, "UART1_TXD",
                java.util.Collections.emptyList(), 42, 0, 10);

        Map<String, java.util.Set<DeviceMode>> modeOverrides = Map.of(
                "P9:42", java.util.Set.of(DeviceMode.DIGITAL_INPUT));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, modeOverrides, Map.of());
        assertEquals("P9", view.getHeader());
        assertEquals(42, view.getPhysicalPin());
        assertEquals(42, view.getDeviceNumber());
        assertEquals("UART1_TXD", view.getName());
        assertEquals(0, view.getChip());
        assertEquals(10, view.getLineOffset());
    }

    @Test
    void loadModes_empty_resource_path() {
        // Empty path should default to classpath resource
        Map<String, java.util.Set<DeviceMode>> overrides = PinModeOverrides.loadModes("");
        assertFalse(overrides.isEmpty(), "Empty path should load from default classpath resource");
    }

    @Test
    void modes_collection_includes_both_original_and_override() {
        PinInfo pinInfo = new PinInfo("test", "P8", 37, 37, "GPIO0",
                java.util.Collections.emptyList());

        Map<String, java.util.Set<DeviceMode>> modeOverrides = Map.of(
                "P8:37", java.util.Set.of(DeviceMode.DIGITAL_INPUT, DeviceMode.DIGITAL_OUTPUT));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, modeOverrides, Map.of());
        Collection<DeviceMode> modes = view.getModes();
        assertTrue(modes.contains(DeviceMode.DIGITAL_INPUT));
        assertTrue(modes.contains(DeviceMode.DIGITAL_OUTPUT));
    }

    @Test
    void wrap_applies_chip_override() {
        PinInfo pinInfo = new PinInfo("kp", "P9", 42, 42, "ECAP0",
                java.util.Collections.emptyList(), 42, 0, 7);

        Map<String, java.util.Set<DeviceMode>> modeOverrides = Map.of();
        Map<String, PinModeOverrides.PinOverride> chipOverrides = Map.of(
                "P9:42", PinModeOverrides.PinOverride.of(3, null));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, modeOverrides, chipOverrides);
        assertEquals("P9", view.getHeader());
        assertEquals(42, view.getPhysicalPin());
        // Chip overridden from 0 → 3
        assertEquals(3, view.getChip());
        // Line offset unchanged
        assertEquals(7, view.getLineOffset());
    }

    @Test
    void wrap_applies_line_offset_override() {
        PinInfo pinInfo = new PinInfo("kp", "P9", 15, 15, "UART1_TXD",
                java.util.Collections.emptyList(), 15, 1, 16);

        Map<String, java.util.Set<DeviceMode>> modeOverrides = Map.of();
        Map<String, PinModeOverrides.PinOverride> chipLineOverrides = Map.of(
                "P9:15", PinModeOverrides.PinOverride.of(1, 0));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, modeOverrides, chipLineOverrides);
        assertEquals("P9", view.getHeader());
        assertEquals(15, view.getPhysicalPin());
        // Chip preserved, line offset overridden from 16 → 0
        assertEquals(1, view.getChip());
        assertEquals(0, view.getLineOffset());
    }

    @Test
    void wrap_with_no_overrides_returns_original_values() {
        PinInfo pinInfo = new PinInfo("kp", "P8", 37, 37, "LCD_DATA8",
                java.util.Collections.emptyList(), 37, 2, 5);

        // No mode or chip overrides
        PinInfoView view = PinModeOverrides.wrap(pinInfo, Map.of(), Map.of());
        assertEquals(2, view.getChip());   // original chip preserved
        assertEquals(5, view.getLineOffset());
    }

    @Test
    void detectKernelVersion_parses_correctly() {
        assertEquals(6, PinModeOverrides.detectKernelVersion("6.6.47-ti-r70"));
        assertEquals(5, PinModeOverrides.detectKernelVersion("5.10.167"));
        assertEquals(4, PinModeOverrides.detectKernelVersion("4.19.94"));
        assertEquals(10, PinModeOverrides.detectKernelVersion("10.0.0"));
        assertEquals(-1, PinModeOverrides.detectKernelVersion(""));
        assertEquals(-1, PinModeOverrides.detectKernelVersion(null));
        assertEquals(-1, PinModeOverrides.detectKernelVersion("unknown"));
    }

    @Test
    void isChipOverrideDisabled_on_kernel_4() {
        // Simulate kernel 4.x by setting os.version system property
        String original = System.getProperty("os.version");
        try {
            System.setProperty("os.version", "4.19.94");
            // Also disable explicit override prop to ensure auto-detect is used
            System.clearProperty("sbc2ha.bbb.chip-override");
            assertFalse(PinModeOverrides.isChipOverrideEnabled(),
                        "Should be disabled on kernel 4.x");
        } finally {
            if (original != null) {
                System.setProperty("os.version", original);
            }
        }
    }

    @Test
    void isChipOverrideEnabled_on_kernel_6() {
        String original = System.getProperty("os.version");
        try {
            System.setProperty("os.version", "6.6.47-ti-r70");
            System.clearProperty("sbc2ha.bbb.chip-override");
            assertTrue(PinModeOverrides.isChipOverrideEnabled(),
                       "Should be enabled on kernel 6.x");
        } finally {
            if (original != null) {
                System.setProperty("os.version", original);
            }
        }
    }

    @Test
    void loadChipOverrides_returns_empty_on_kernel_4() {
        String original = System.getProperty("os.version");
        try {
            System.setProperty("os.version", "4.19.94");
            System.clearProperty("sbc2ha.bbb.chip-override");
            Map<String, PinModeOverrides.PinOverride> overrides = PinModeOverrides.loadChipOverrides("");
            assertTrue(overrides.isEmpty(), "Should return empty on kernel 4.x");
        } finally {
            if (original != null) {
                System.setProperty("os.version", original);
            }
        }
    }

    // ── Full 6-column chip mapping tests ────────────────────────────────

    @Test
    void loadChipOverrides_kernel6_loads_full_format() {
        String original = System.getProperty("os.version");
        try {
            System.setProperty("os.version", "6.18.34-bone37");
            System.clearProperty("sbc2ha.bbb.chip-override");
            Map<String, PinModeOverrides.PinOverride> overrides = PinModeOverrides.loadChipOverrides("");
            assertFalse(overrides.isEmpty(), "Should load chip overrides on kernel 6.x");
            // P8:37: diozero chip=2, line=14 → kernel chip=1, line=14
            PinModeOverrides.PinOverride p8_37 = overrides.get("P8:37");
            assertNotNull(p8_37, "P8:37 should have a chip override");
            assertEquals(1, p8_37.chip(), "P8:37 chip should be 1 (kernel 6.x)");
            assertEquals(14, p8_37.lineOffset(), "P8:37 line should be 14");
            // P9:11: diozero chip=0, line=30 → kernel chip=3, line=30
            PinModeOverrides.PinOverride p9_11 = overrides.get("P9:11");
            assertNotNull(p9_11, "P9:11 should have a chip override");
            assertEquals(3, p9_11.chip(), "P9:11 chip should be 3 (kernel 6.x)");
            assertEquals(30, p9_11.lineOffset(), "P9:11 line should be 30");
            System.out.println("Loaded " + overrides.size() + " chip overrides on kernel 6.x");
        } finally {
            if (original != null) {
                System.setProperty("os.version", original);
            }
        }
    }

    @Test
    void loadChipOverrides_skips_entries_with_no_difference() {
        // P9:15 has line-only override (chip stays 1, line 16→0) but is commented out
        // in the generated file. Only entries where chip OR line differs are included.
        String original = System.getProperty("os.version");
        try {
            System.setProperty("os.version", "6.18.34-bone37");
            System.clearProperty("sbc2ha.bbb.chip-override");
            Map<String, PinModeOverrides.PinOverride> overrides = PinModeOverrides.loadChipOverrides("");
            // P9:15 is commented out → should NOT be present
            assertNull(overrides.get("P9:15"),
                       "P9:15 (line-only, commented out) should not be present");
        } finally {
            if (original != null) {
                System.setProperty("os.version", original);
            }
        }
    }

    @Test
    void wrap_applies_full_chip_line_override() {
        // Simulate a pin that has both chip and line offset differences
        PinInfo pinInfo = new PinInfo("kp", "P8", 37, 37, "LCD_DATA8",
                java.util.Collections.emptyList(), 37, 2, 14);

        Map<String, PinModeOverrides.PinOverride> chipLineOverrides = Map.of(
                "P8:37", PinModeOverrides.PinOverride.of(1, 14));

        PinInfoView view = PinModeOverrides.wrap(pinInfo, Map.of(), chipLineOverrides);
        assertEquals("P8", view.getHeader());
        assertEquals(37, view.getPhysicalPin());
        // Chip overridden from 2 → 1
        assertEquals(1, view.getChip());
        // Line offset preserved (14 → 14, same value)
        assertEquals(14, view.getLineOffset());
    }

    @Test
    void loadChipOverrides_respects_explicit_disable() {
        String original = System.getProperty("os.version");
        try {
            System.setProperty("os.version", "6.18.34-bone37");
            System.setProperty("sbc2ha.bbb.chip-override", "false");
            Map<String, PinModeOverrides.PinOverride> overrides = PinModeOverrides.loadChipOverrides("");
            assertTrue(overrides.isEmpty(),
                       "Explicit false should disable overrides regardless of kernel version");
        } finally {
            if (original != null) {
                System.setProperty("os.version", original);
            }
        }
    }
}
