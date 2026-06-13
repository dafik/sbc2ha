package iot.sbc2ha.hardware.io.diozero;

import com.diozero.devices.MCP23017;
import com.diozero.devices.mcp23xxx.MCP23xxx;
import iot.sbc2ha.hardware.io.OutputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DiozeroOutputAdapter}.
 *
 * <p>Tests inversion logic, write/read semantics, and state tracking.
 * String parsing is tested in {@link DiozeroInputOutputFactoryTest}
 * since the adapter no longer knows about I2C addresses or pins.</p>
 */
@DisplayName("DiozeroOutputAdapter")
class DiozeroOutputAdapterTest {

    @BeforeAll
    static void setUp() {
        System.setProperty("diozero.devicefactory", "com.diozero.internal.provider.mock.MockDeviceFactory");
    }

    @AfterEach
    void tearDown() {
        HardwareComponentRegistry.INSTANCE.clear();
    }

    // -----------------------------------------------------------------------
    // Helper: create an adapter with a pre-built MCP23017 chip
    // -----------------------------------------------------------------------

    private DiozeroOutputAdapter createAdapter(int i2cBus, int i2cAddress, int globalPin, boolean inverted) {
        HardwareComponentKey chipKey = new HardwareComponentKey("mcp23017", i2cBus + ":" + i2cAddress);
        MCP23017 mcp = new MCP23017(i2cBus, i2cAddress, MCP23xxx.INTERRUPT_GPIO_NOT_SET);
        HardwareComponentRegistry.INSTANCE.getOrRegister(chipKey, () -> mcp);
        OutputDelegate delegate = OutputDelegateFactory.create(mcp, globalPin);
        return new DiozeroOutputAdapter(inverted, delegate);
    }

    // -----------------------------------------------------------------------
    // Write semantics
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Write semantics")
    class WriteSemantics {

        @Test
        @DisplayName("write(ON) sets logical state to ON")
        void writeOnSetsState() {
            DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 0, false);
            adapter.write(DeviceState.ON);
            assertEquals(DeviceState.ON, adapter.getState());
        }

        @Test
        @DisplayName("write(OFF) sets logical state to OFF")
        void writeOffSetsState() {
            DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 5, false);
            adapter.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, adapter.getState());
        }

        @Test
        @DisplayName("write toggles state between ON and OFF")
        void writeToggles() {
            DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 3, false);
            adapter.write(DeviceState.ON);
            assertEquals(DeviceState.ON, adapter.getState());
            adapter.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, adapter.getState());
            adapter.write(DeviceState.ON);
            assertEquals(DeviceState.ON, adapter.getState());
        }
    }

    // -----------------------------------------------------------------------
    // Inversion
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Inversion")
    class Inversion {

        @Test
        @DisplayName("non-inverted write(ON) sets state to ON")
        void nonInvertedWriteOn() {
            DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 0, false);
            adapter.write(DeviceState.ON);
            assertEquals(DeviceState.ON, adapter.getState());
        }

        @Test
        @DisplayName("inverted write(ON) still sets logical state to ON")
        void invertedWriteOn() {
            DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 0, true);
            adapter.write(DeviceState.ON);
            // Logical state is ON (physical would be LOW/0)
            assertEquals(DeviceState.ON, adapter.getState());
        }

        @Test
        @DisplayName("inverted write(OFF) sets logical state to OFF")
        void invertedWriteOff() {
            DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 0, true);
            adapter.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, adapter.getState());
        }
    }

    // -----------------------------------------------------------------------
    // toString / getState
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toString includes adapter class name")
    void toStringContainsInfo() {
        DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 3, false);
        String s = adapter.toString();
        assertTrue(s.contains("DiozeroOutputAdapter"));
    }

    @Test
    @DisplayName("getState returns last written state")
    void getStateReturnsLastState() {
        DiozeroOutputAdapter adapter = createAdapter(1, 0x20, 7, false);
        assertEquals(DeviceState.OFF, adapter.getState()); // default from OLAT read
        adapter.write(DeviceState.ON);
        assertEquals(DeviceState.ON, adapter.getState());
    }

    // -----------------------------------------------------------------------
    // Chip sharing (registry singleton per bus+addr)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Chip sharing")
    class ChipSharing {

        @Test
        @DisplayName("adapters on same chip share the same registry entry")
        void adaptersShareChipInstance() {
            DiozeroOutputAdapter a1 = createAdapter(1, 0x20, 0, false);
            DiozeroOutputAdapter a2 = createAdapter(1, 0x20, 5, false);

            var key = new HardwareComponentKey("mcp23017", "1:32");
            assertNotNull(HardwareComponentRegistry.INSTANCE.getComponent(key));
        }

        @Test
        @DisplayName("adapters on different chips have separate registry entries")
        void differentChipsSeparateEntries() {
            DiozeroOutputAdapter a1 = createAdapter(1, 0x20, 0, false);
            DiozeroOutputAdapter a2 = createAdapter(1, 0x21, 0, false);

            var key0x20 = new HardwareComponentKey("mcp23017", "1:32");
            var key0x21 = new HardwareComponentKey("mcp23017", "1:33");
            assertNotNull(HardwareComponentRegistry.INSTANCE.getComponent(key0x20));
            assertNotNull(HardwareComponentRegistry.INSTANCE.getComponent(key0x21));
        }
    }
}
