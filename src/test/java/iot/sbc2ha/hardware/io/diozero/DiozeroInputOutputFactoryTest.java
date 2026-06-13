package iot.sbc2ha.hardware.io.diozero;

import iot.sbc2ha.hardware.GpioChannel;
import iot.sbc2ha.hardware.HardwareChip;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.Mcp23017Channel;
import iot.sbc2ha.hardware.PhysicalChannel;
import iot.sbc2ha.hardware.io.OutputAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DiozeroInputOutputFactory}.
 *
 * <p>Covers dispatch by {@link PhysicalChannel#channelType()} and MCP23017
 * channel validation. The adapter no longer carries I2C metadata — the
 * factory dispatches based on channel type and resolves chips from the model.</p>
 */
@DisplayName("DiozeroInputOutputFactory")
class DiozeroInputOutputFactoryTest {

    @BeforeAll
    static void setUp() {
        System.setProperty("diozero.devicefactory", "com.diozero.internal.provider.mock.MockDeviceFactory");
    }

    @AfterEach
    void tearDown() {
        HardwareComponentRegistry.INSTANCE.clear();
    }

    // -----------------------------------------------------------------------
    // MCP23017 channels
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("MCP23017 channels")
    class Mcp23017Channels {

        private HardwareModel modelWithMcp1() {
            return new HardwareModel("test", null,
                    List.of(new HardwareChip("mcp1", "mcp23017", 0x20)),
                    List.of(), List.of());
        }

        @Test
        @DisplayName("creates adapter for port A")
        void createsAdapterForPortA() {
            Mcp23017Channel ch = new Mcp23017Channel("mcp1", 0);
            OutputAdapter adapter = DiozeroInputOutputFactory.INSTANCE
                    .createOutput(ch, modelWithMcp1());
            assertNotNull(adapter);
            assertInstanceOf(DiozeroOutputAdapter.class, adapter);
        }

        @Test
        @DisplayName("creates adapter for port B")
        void createsAdapterForPortB() {
            Mcp23017Channel ch = new Mcp23017Channel("mcp1", 15);
            OutputAdapter adapter = DiozeroInputOutputFactory.INSTANCE
                    .createOutput(ch, modelWithMcp1());
            assertNotNull(adapter);
        }

        @Test
        @DisplayName("throws on unknown chip id")
        void rejectsUnknownChip() {
            Mcp23017Channel ch = new Mcp23017Channel("unknown_chip", 0);
            assertThrows(IllegalArgumentException.class,
                    () -> DiozeroInputOutputFactory.INSTANCE
                            .createOutput(ch, modelWithMcp1()));
        }

        @Test
        @DisplayName("throws on invalid pin (>15)")
        void rejectsInvalidPin() {
            assertThrows(IllegalArgumentException.class, () -> {
                Mcp23017Channel ch = new Mcp23017Channel("mcp1", 16);
                DiozeroInputOutputFactory.INSTANCE.createOutput(ch, modelWithMcp1());
            });
        }
    }

    // -----------------------------------------------------------------------
    // GPIO channels
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GPIO channels")
    class GpioChannels {

        @Test
        @DisplayName("throws for unknown GPIO pin on mock board")
        void rejectsUnknownGpioPin() {
            GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.OUTPUT);
            // Mock board has no pre-populated pins → resolvePin returns null
            assertThrows(IllegalArgumentException.class,
                    () -> DiozeroInputOutputFactory.INSTANCE.createOutput(ch));
        }
    }

    // -----------------------------------------------------------------------
    // Unsupported channel types
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Unsupported types")
    class UnsupportedTypes {

        @Test
        @DisplayName("throws for unknown GPIO pin on mock board")
        void rejectsUnknownGpioPin() {
            GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.OUTPUT);
            assertThrows(IllegalArgumentException.class,
                    () -> DiozeroInputOutputFactory.INSTANCE.createOutput(ch));
        }
    }

    // -----------------------------------------------------------------------
    // Chip sharing
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Chip sharing")
    class ChipSharing {

        private HardwareModel modelWithMcp1() {
            return new HardwareModel("test", null,
                    List.of(new HardwareChip("mcp1", "mcp23017", 0x20)),
                    List.of(), List.of());
        }

        @Test
        @DisplayName("multiple adapters on same chip share registry entry")
        void adaptersShareChip() {
            Mcp23017Channel ch1 = new Mcp23017Channel("mcp1", 0);
            Mcp23017Channel ch2 = new Mcp23017Channel("mcp1", 5);
            HardwareModel model = modelWithMcp1();

            DiozeroInputOutputFactory.INSTANCE.createOutput(ch1, model);
            DiozeroInputOutputFactory.INSTANCE.createOutput(ch2, model);

            var key = new HardwareComponentKey("mcp23017", "2:32");
            assertNotNull(HardwareComponentRegistry.INSTANCE.getComponent(key));
            assertEquals(1, HardwareComponentRegistry.INSTANCE.size());
        }

        @Test
        @DisplayName("different chips get separate registry entries")
        void differentChipsSeparate() {
            HardwareModel model = new HardwareModel("test", null,
                    List.of(
                            new HardwareChip("mcp1", "mcp23017", 0x20),
                            new HardwareChip("mcp2", "mcp23017", 0x21)
                    ),
                    List.of(), List.of());

            Mcp23017Channel ch1 = new Mcp23017Channel("mcp1", 0);
            Mcp23017Channel ch2 = new Mcp23017Channel("mcp2", 0);

            DiozeroInputOutputFactory.INSTANCE.createOutput(ch1, model);
            DiozeroInputOutputFactory.INSTANCE.createOutput(ch2, model);

            assertEquals(2, HardwareComponentRegistry.INSTANCE.size());
        }
    }
}
