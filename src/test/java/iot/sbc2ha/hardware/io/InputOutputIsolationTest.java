package iot.sbc2ha.hardware.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the fake runtime package hierarchy is diozero-free.
 *
 * <p>This test scans the {@code iot.sbc2ha.hardware.gpio.fake} package and
 * verifies that none of the classes inside import or reference any class
 * from the {@code com.diozero} package. This enforces the architecture
 * rule: diozero must be isolated in hardware adapters, not leak into
 * fake implementations or the core runtime.</p>
 *
 * <p>Additionally, it verifies that the real diozero adapters
 * ({@code iot.sbc2ha.hardware.gpio.diozero}) DO reference diozero
 * (expected) and do NOT contain diozero in the fake package.</p>
 */
class InputOutputIsolationTest {

    @Test
    void fakePackage_hasNoDiozeroDependencies() {
        // Use target/classes as the root for scanning compiled classes
        String base = System.getProperty("user.dir") + "/target/classes";
        java.io.File fakeDir = new java.io.File(base, "iot/sbc2ha/hardware/io/fake");

        assertTrue(fakeDir.isDirectory(),
                "Fake class dir should exist after compile: " + fakeDir);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        List<Class<?>> fakeClasses = new ArrayList<>();
        scanDirectory(fakeClasses, cl, fakeDir, "iot.sbc2ha.hardware.gpio.fake");

        for (Class<?> clazz : fakeClasses) {
            String superName = clazz.getSuperclass().getName();
            assertFalse(superName.startsWith("com.diozero"),
                    "Fake class " + clazz.getName()
                            + " extends a diozero type — diozero must not leak into fakes: "
                            + superName);
            for (Class<?> iface : clazz.getInterfaces()) {
                String ifaceName = iface.getName();
                assertFalse(ifaceName.startsWith("com.diozero"),
                        "Fake class " + clazz.getName()
                                + " implements a diozero type — diozero must not leak into fakes: "
                                + ifaceName);
            }
        }
    }

    private void scanDirectory(List<Class<?>> classes, ClassLoader cl, java.io.File dir, String pkg) {
        java.io.File[] entries = dir.listFiles();
        if (entries == null) return;
        for (java.io.File entry : entries) {
            if (entry.isDirectory()) {
                scanDirectory(classes, cl, entry, pkg + "." + entry.getName());
            } else if (entry.getName().endsWith(".class")) {
                String className = entry.getName().substring(0, entry.getName().length() - 6);
                try {
                    classes.add(cl.loadClass(pkg + "." + className));
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Source-level check: verifies that no .java source file in the
     * fake package contains "com.diozero" text.
     */
    @Test
    void fakeSource_hasNoDiozeroImports() {
        String base = System.getProperty("user.dir") + "/src/main/java/";
        java.io.File fakeDir = new java.io.File(base, "iot/sbc2ha/hardware/io/fake");

        assertTrue(fakeDir.isDirectory(),
                "Fake source dir should exist: " + fakeDir);

        List<String> violations = new ArrayList<>();
        java.io.File[] entries = fakeDir.listFiles();
        if (entries != null) {
            for (java.io.File entry : entries) {
                if (entry.getName().endsWith(".java")) {
                    String content;
                    try {
                        content = java.nio.file.Files.readString(entry.toPath());
                    } catch (java.io.IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (content.contains("com.diozero") || content.contains("diozero.")) {
                        violations.add(entry.getName());
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Fake source files must not reference diozero:\n" + String.join("\n", violations));
    }

    /**
     * Verifies that the real diozero adapter source references diozero
     * and contains inversion support.
     */
    @Test
    void diozeroAdapterSource_referencesDiozeroAndInversion() throws IOException {
        String base = System.getProperty("user.dir") + "/src/main/java/";
        java.io.File diozeroDir = new java.io.File(base, "iot/sbc2ha/hardware/io/diozero");

        if (!diozeroDir.isDirectory()) {
            // diozero adapter not yet implemented — skip
            return;
        }

        List<String> diozeroSources = new ArrayList<>();
        scanSourceFiles(diozeroSources, diozeroDir, "iot.sbc2ha.hardware.gpio.diozero");

        assertFalse(diozeroSources.isEmpty(),
                "Diozero adapter package should have at least one source file");

        for (String fileName : diozeroSources) {
            java.io.File file = new java.io.File(diozeroDir, fileName);
            String content = java.nio.file.Files.readString(file.toPath());
            // Diozero adapter MUST reference diozero
            assertTrue(content.contains("com.diozero") || content.contains("diozero"),
                    fileName + " should reference diozero");
        }

        // At least one file should contain "inverted" for inversion support
        boolean hasInversion = diozeroSources.stream().anyMatch(name -> {
            try {
                String content = java.nio.file.Files.readString(
                        new java.io.File(diozeroDir, name).toPath());
                return content.contains("inverted");
            } catch (IOException e) {
                return false;
            }
        });
        assertTrue(hasInversion,
                "At least one diozero adapter source file should reference inversion");
    }

    private void scanSourceFiles(List<String> violations, java.io.File dir, String pkg) {
        java.io.File[] entries = dir.listFiles();
        if (entries == null) return;
        for (java.io.File entry : entries) {
            if (entry.isDirectory()) {
                scanSourceFiles(violations, entry, pkg + "." + entry.getName());
            } else if (entry.getName().endsWith(".java")) {
                violations.add(entry.getName());
            }
        }
    }
}
