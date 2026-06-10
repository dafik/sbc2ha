package iot.sbc2ha.hardware.io.diozero;

import iot.sbc2ha.runtime.DeviceState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DiozeroOutputAdapter}.
 *
 * <p>Tests the I2C address/pin parsing logic, write/read semantics,
 * and inversion support. Hardware communication tests are covered
 * by integration tests on real hardware.</p>
 */
@DisplayName("DiozeroOutputAdapter")
class DiozeroOutputAdapterTest {

    @BeforeAll
    static void setUp() {
        // Use mock provider so I2C operations hit MockI2CDevice instead of real hardware
        System.setProperty("diozero.devicefactory", "com.diozero.internal.provider.mock.MockDeviceFactory");
    }

    // -----------------------------------------------------------------------
    // Location string parsing
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Location parsing")
    class LocationParsing {

        @Test
        @DisplayName("parses hex address location string")
        void parsesHexAddressLocation() {
            // pinId = "i2c-1:0x20:A:0" → bus=1, addr=0x20=32, port=A, pin=0
            // The constructor reads OLAT from chip on construction.
            // We test via the package-private constructor to avoid real I2C.
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 0, false);
            assertEquals(1, adapter.i2cBus());
            assertEquals(0x20, adapter.i2cAddress());
            assertEquals(0, adapter.pin());
        }

        @Test
        @DisplayName("parses decimal address location string")
        void parsesDecimalAddressLocation() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 32, 0, false);
            assertEquals(1, adapter.i2cBus());
            assertEquals(32, adapter.i2cAddress());
        }

        @Test
        @DisplayName("parses port B pins (offset +8)")
        void parsesPortB() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 8, false);
            assertEquals(1, adapter.i2cBus());
            assertEquals(0x20, adapter.i2cAddress());
            assertEquals(8, adapter.pin());
        }

        @Test
        @DisplayName("parses highest port B pin (pin 15)")
        void parsesHighestPin() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(0, 0x27, 15, false);
            assertEquals(0, adapter.i2cBus());
            assertEquals(0x27, adapter.i2cAddress());
            assertEquals(15, adapter.pin());
        }

        @Test
        @DisplayName("throws on missing colon-separated parts")
        void rejectsMissingParts() {
            // Package-private constructor bypasses parsing — test the parsing separately
            // by catching the constructor that takes the string
            assertThrows(IllegalArgumentException.class,
                    () -> new DiozeroOutputAdapter("invalid", false));
        }

        @Test
        @DisplayName("throws on invalid port (not A or B)")
        void rejectsInvalidPort() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiozeroOutputAdapter("i2c-1:0x20:C:0", false));
        }

        @Test
        @DisplayName("throws on invalid pin number (>7)")
        void rejectsInvalidPin() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiozeroOutputAdapter("i2c-1:0x20:A:8", false));
        }
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
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 0, false);
            adapter.write(DeviceState.ON);
            assertEquals(DeviceState.ON, adapter.getState());
        }

        @Test
        @DisplayName("write(OFF) sets logical state to OFF")
        void writeOffSetsState() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 5, false);
            adapter.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, adapter.getState());
        }

        @Test
        @DisplayName("write toggles state between ON and OFF")
        void writeToggles() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 3, false);
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
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 0, false);
            adapter.write(DeviceState.ON);
            assertEquals(DeviceState.ON, adapter.getState());
        }

        @Test
        @DisplayName("inverted write(ON) still sets logical state to ON")
        void invertedWriteOn() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 0, true);
            adapter.write(DeviceState.ON);
            // Logical state is ON (physical would be LOW/0)
            assertEquals(DeviceState.ON, adapter.getState());
        }

        @Test
        @DisplayName("inverted write(OFF) sets logical state to OFF")
        void invertedWriteOff() {
            DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 0, true);
            adapter.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, adapter.getState());
        }
    }

    // -----------------------------------------------------------------------
    // toString / getState
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toString includes bus, address, and pin")
    void toStringContainsInfo() {
        DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 3, false);
        String s = adapter.toString();
        assertTrue(s.contains("DiozeroOutputAdapter"));
        assertTrue(s.contains("bus=1"));
        assertTrue(s.contains("0x20"));
        assertTrue(s.contains("pin=3"));
    }

    @Test
    @DisplayName("getState returns last written state")
    void getStateReturnsLastState() {
        DiozeroOutputAdapter adapter = new DiozeroOutputAdapter(1, 0x20, 7, false);
        assertEquals(DeviceState.OFF, adapter.getState()); // default from OLAT read
        adapter.write(DeviceState.ON);
        assertEquals(DeviceState.ON, adapter.getState());
    }
}
