package iot.sbc2ha.boot;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OledBootDisplay}.
 *
 * <p>Uses the diozero mock provider so no real I2C hardware is needed.
 * Tests verify fail-open behaviour: construction, update, and close
 * must never throw or block the boot sequence.</p>
 */
@DisplayName("OledBootDisplay")
class OledBootDisplayTest {

    @BeforeAll
    static void setUp() {
        // Use mock provider so OledDevice hits MockOledDevice instead of real hardware
        System.setProperty("diozero.devicefactory", "com.diozero.internal.provider.mock.MockDeviceFactory");
    }

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("default constructor creates display on bus 1, addr 0x3C")
        void defaultConstructor() {
            assertDoesNotThrow(() -> new OledBootDisplay());
        }

        @Test
        @DisplayName("explicit constructor creates display on given bus/address")
        void explicitConstructor() {
            assertDoesNotThrow(() -> new OledBootDisplay(0, 0x3C));
        }

        @Test
        @DisplayName("constructor never throws even if I2C hardware is absent")
        void constructionNeverThrows() {
            // If mock provider is active, this succeeds; if not, still fail-open
            assertDoesNotThrow(() -> new OledBootDisplay(99, 0xFF));
        }
    }

    // -----------------------------------------------------------------------
    // update() fail-open
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("update(BOOTING) does not throw")
        void updateBootting() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(() -> display.update(LifecycleState.BOOTING));
        }

        @Test
        @DisplayName("update(OFFLINE_READY) does not throw")
        void updateOfflineReady() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(() -> display.update(LifecycleState.OFFLINE_READY));
        }

        @Test
        @DisplayName("update(CONFIG_ERROR) does not throw")
        void updateConfigError() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(() -> display.update(LifecycleState.CONFIG_ERROR));
        }

        @Test
        @DisplayName("multiple updates succeed")
        void multipleUpdates() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(() -> {
                display.update(LifecycleState.BOOTING);
                display.update(LifecycleState.CONFIG_LOADED);
                display.update(LifecycleState.STATE_RESTORED);
                display.update(LifecycleState.OFFLINE_READY);
            });
        }

        @Test
        @DisplayName("update with null display object (init failure) does not throw")
        void updateWithNullOled() {
            // Force construction failure by using invalid bus/address combo
            // The mock provider may still work, but the fail-open path
            // checks oled != null before rendering
            OledBootDisplay display = new OledBootDisplay(99, 0xFF);
            // Even if the oled was created, calling update should not throw
            assertDoesNotThrow(() -> display.update(LifecycleState.ERROR));
        }
    }

    // -----------------------------------------------------------------------
    // close() fail-open
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("close does not throw")
        void closeNoThrow() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(display::close);
        }

        @Test
        @DisplayName("close after update does not throw")
        void closeAfterUpdate() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(() -> {
                display.update(LifecycleState.OFFLINE_READY);
                display.close();
            });
        }

        @Test
        @DisplayName("double close does not throw")
        void doubleClose() {
            OledBootDisplay display = new OledBootDisplay();
            assertDoesNotThrow(() -> {
                display.close();
                display.close();
            });
        }
    }

    // -----------------------------------------------------------------------
    // Integration with Lifecycle
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Lifecycle integration")
    class LifecycleIntegration {

        @Test
        @DisplayName("Lifecycle uses OledBootDisplay without blocking")
        void lifecycleWithOledDisplay() {
            OledBootDisplay display = new OledBootDisplay();
            Lifecycle lifecycle = new Lifecycle(display);

            assertEquals(LifecycleState.BOOTING, lifecycle.state());
            assertDoesNotThrow(() -> lifecycle.transition(LifecycleState.CONFIG_LOADED));
            assertEquals(LifecycleState.CONFIG_LOADED, lifecycle.state());
            assertDoesNotThrow(() -> lifecycle.transition(LifecycleState.OFFLINE_READY));
            assertEquals(LifecycleState.OFFLINE_READY, lifecycle.state());
            assertDoesNotThrow(lifecycle::shutdown);
        }

        @Test
        @DisplayName("Lifecycle transition with OledBootDisplay works through full chain")
        void fullTransitionChain() {
            OledBootDisplay display = new OledBootDisplay();
            Lifecycle lifecycle = new Lifecycle(display);

            assertDoesNotThrow(() -> {
                lifecycle.transition(LifecycleState.CONFIG_LOADED);
                lifecycle.transition(LifecycleState.STATE_RESTORED);
                lifecycle.transition(LifecycleState.OFFLINE_READY);
            });

            assertEquals(LifecycleState.OFFLINE_READY, lifecycle.state());
            assertDoesNotThrow(lifecycle::shutdown);
        }
    }
}
