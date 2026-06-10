package iot.sbc2ha.hardware.gpio;

import org.junit.jupiter.api.Test;

import java.io.IOException;
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
 */
class DiozeroIsolationTest {

    private static final String FAKE_PKG = "iot.sbc2ha.hardware.gpio.fake";

    @Test
    void fakePackage_hasNoDiozeroDependencies() {
        // Use target/classes as the root for scanning compiled classes
        String base = System.getProperty("user.dir") + "/target/classes";
        java.io.File fakeDir = new java.io.File(base, "iot/sbc2ha/hardware/gpio/fake");

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
    void fakeSource_hasNoDiozeroImports() throws IOException {
        String base = System.getProperty("user.dir") + "/src/main/java/";
        java.io.File fakeDir = new java.io.File(base, "iot/sbc2ha/hardware/gpio/fake");

        assertTrue(fakeDir.isDirectory(),
                "Fake source dir should exist: " + fakeDir);

        List<String> violations = new ArrayList<>();
        scanSourceFiles(violations, fakeDir, FAKE_PKG);

        assertTrue(violations.isEmpty(),
                "Fake source files must not reference diozero:\n" + String.join("\n", violations));
    }

    private void scanSourceFiles(List<String> violations, java.io.File dir, String pkg)
            throws IOException {
        java.io.File[] entries = dir.listFiles();
        if (entries == null) return;
        for (java.io.File entry : entries) {
            if (entry.isDirectory()) {
                scanSourceFiles(violations, entry, pkg + "." + entry.getName());
            } else if (entry.getName().endsWith(".java")) {
                String content = java.nio.file.Files.readString(entry.toPath());
                if (content.contains("com.diozero") || content.contains("diozero.")) {
                    violations.add(entry.getName() + ": contains 'diozero' reference");
                }
            }
        }
    }
}
