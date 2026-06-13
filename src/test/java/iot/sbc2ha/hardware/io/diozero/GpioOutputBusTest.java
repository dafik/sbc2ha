package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DeviceMode;
import com.diozero.api.PinInfo;
import iot.sbc2ha.hardware.io.OutputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GpioOutputBus} delegate.
 *
 * <p>Uses diozero's mock provider with a manually constructed {@link PinInfo}
 * because the mock board has no pre-populated pins.</p>
 */
@DisplayName("GpioOutputBus")
class GpioOutputBusTest {

    @BeforeAll
    static void setUp() {
        System.setProperty("diozero.devicefactory", "com.diozero.internal.provider.mock.MockDeviceFactory");
    }

    private PinInfo createPinInfo(int deviceNumber) {
        return new PinInfo("GPIO", "P9", deviceNumber, 12, "P9_12",
                EnumSet.of(DeviceMode.DIGITAL_OUTPUT, DeviceMode.DIGITAL_INPUT));
    }

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("creates delegate from PinInfo")
        void createsFromPinInfo() {
            PinInfo pinInfo = createPinInfo(0);
            OutputDelegate delegate = OutputDelegateFactory.create(pinInfo, "P9_12");
            assertInstanceOf(GpioOutputBus.class, delegate);
        }

        @Test
        @DisplayName("throws on null PinInfo")
        void rejectsNullPinInfo() {
            assertThrows(IllegalArgumentException.class,
                    () -> OutputDelegateFactory.create(null, "P9_12"));
        }
    }

    // -----------------------------------------------------------------------
    // Write / Read
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Write and Read")
    class WriteAndRead {

        @Test
        @DisplayName("write(ON) sets state to ON")
        void writeOnSetsState() {
            GpioOutputBus bus = new GpioOutputBus("P9_12", createPinInfo(0));
            bus.write(DeviceState.ON);
            assertEquals(DeviceState.ON, bus.getState());
        }

        @Test
        @DisplayName("write(OFF) sets state to OFF")
        void writeOffSetsState() {
            GpioOutputBus bus = new GpioOutputBus("P9_12", createPinInfo(0));
            bus.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, bus.getState());
        }

        @Test
        @DisplayName("write toggles between ON and OFF")
        void writeToggles() {
            GpioOutputBus bus = new GpioOutputBus("P9_12", createPinInfo(0));
            bus.write(DeviceState.ON);
            assertEquals(DeviceState.ON, bus.getState());
            bus.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, bus.getState());
            bus.write(DeviceState.ON);
            assertEquals(DeviceState.ON, bus.getState());
        }

        @Test
        @DisplayName("read() returns last written state")
        void readReturnsLastState() {
            GpioOutputBus bus = new GpioOutputBus("P9_12", createPinInfo(0));
            assertEquals(DeviceState.OFF, bus.read()); // initial
            bus.write(DeviceState.ON);
            assertEquals(DeviceState.ON, bus.read());
            bus.write(DeviceState.OFF);
            assertEquals(DeviceState.OFF, bus.read());
        }
    }

    // -----------------------------------------------------------------------
    // Close
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("close() releases resources without throwing")
    void closeDoesNotThrow() {
        GpioOutputBus bus = new GpioOutputBus("P9_12", createPinInfo(1));
        assertDoesNotThrow(bus::close);
    }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toString includes pin ID")
    void toStringContainsPinId() {
        GpioOutputBus bus = new GpioOutputBus("P9_12", createPinInfo(2));
        String s = bus.toString();
        assertTrue(s.contains("GpioOutputBus"));
        assertTrue(s.contains("P9_12"));
    }
}
