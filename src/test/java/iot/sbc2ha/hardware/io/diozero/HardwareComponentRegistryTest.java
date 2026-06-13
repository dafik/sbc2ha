package iot.sbc2ha.hardware.io.diozero;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HardwareComponentRegistry}.
 */
@DisplayName("HardwareComponentRegistry")
class HardwareComponentRegistryTest {

    @BeforeEach
    void setUp() {
        // Clear registry between tests
        HardwareComponentRegistry.INSTANCE.clear();
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("registers and retrieves a component")
        void registersAndRetrieves() {
            var key = new HardwareComponentKey("mcp23017", "2:32");
            Object component = new Object();

            Object result = HardwareComponentRegistry.INSTANCE.register(key, component);
            assertSame(component, result);
            assertSame(component, HardwareComponentRegistry.INSTANCE.getComponent(key));
        }

        @Test
        @DisplayName("throws on duplicate registration")
        void rejectsDuplicate() {
            var key = new HardwareComponentKey("mcp23017", "2:32");
            Object component = new Object();

            HardwareComponentRegistry.INSTANCE.register(key, component);
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> HardwareComponentRegistry.INSTANCE.register(key, new Object()));
            assertTrue(ex.getMessage().contains("already registered"));
        }

        @Test
        @DisplayName("getOrRegister returns existing component without calling factory")
        void getOrRegisterReturnsExisting() {
            var key = new HardwareComponentKey("mcp23017", "2:32");
            Object component = new Object();

            HardwareComponentRegistry.INSTANCE.register(key, component);

            boolean[] factoryCalled = {false};
            Object result = HardwareComponentRegistry.INSTANCE.getOrRegister(key,
                    () -> { factoryCalled[0] = true; return new Object(); });
            assertSame(component, result);
            assertFalse(factoryCalled[0], "factory should not be called for existing component");
        }

        @Test
        @DisplayName("getOrRegister calls factory and caches when missing")
        void getOrRegisterCallsFactory() {
            var key = new HardwareComponentKey("mcp23017", "2:32");
            Object component = new Object();

            Object result = HardwareComponentRegistry.INSTANCE.getOrRegister(key,
                    () -> component);
            assertSame(component, result);
            assertSame(component, HardwareComponentRegistry.INSTANCE.getComponent(key));
        }
    }

    @Nested
    @DisplayName("Size and keys")
    class SizeAndKeys {

        @Test
        @DisplayName("size reflects number of registered components")
        void sizeReflectsCount() {
            assertEquals(0, HardwareComponentRegistry.INSTANCE.size());

            HardwareComponentRegistry.INSTANCE.register(
                    new HardwareComponentKey("mcp23017", "2:32"), new Object());
            assertEquals(1, HardwareComponentRegistry.INSTANCE.size());

            HardwareComponentRegistry.INSTANCE.register(
                    new HardwareComponentKey("mcp23017", "2:33"), new Object());
            assertEquals(2, HardwareComponentRegistry.INSTANCE.size());
        }

        @Test
        @DisplayName("keys returns all registered keys")
        void keysReturnsAll() {
            var k1 = new HardwareComponentKey("mcp23017", "2:32");
            var k2 = new HardwareComponentKey("pca9685", "2:40");

            HardwareComponentRegistry.INSTANCE.register(k1, new Object());
            HardwareComponentRegistry.INSTANCE.register(k2, new Object());

            assertEquals(2, HardwareComponentRegistry.INSTANCE.keys().size());
            assertTrue(HardwareComponentRegistry.INSTANCE.keys().contains(k1));
            assertTrue(HardwareComponentRegistry.INSTANCE.keys().contains(k2));
        }
    }

    @Nested
    @DisplayName("Clear")
    class Clear {

        @Test
        @DisplayName("clears all registered components")
        void clearsAll() {
            HardwareComponentRegistry.INSTANCE.register(
                    new HardwareComponentKey("mcp23017", "2:32"), new Object());
            HardwareComponentRegistry.INSTANCE.register(
                    new HardwareComponentKey("pca9685", "2:40"), new Object());

            HardwareComponentRegistry.INSTANCE.clear();

            assertEquals(0, HardwareComponentRegistry.INSTANCE.size());
            assertNull(HardwareComponentRegistry.INSTANCE.getComponent(
                    new HardwareComponentKey("mcp23017", "2:32")));
        }
    }
}
