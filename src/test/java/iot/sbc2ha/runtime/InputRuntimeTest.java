package iot.sbc2ha.runtime;

import iot.sbc2ha.device.InputDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputRuntimeTest {

    @Test
    void inputRuntime_createdDefault_initializesOff() {
        InputDevice dev = new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false);
        InputRuntime runtime = new InputRuntime(dev);

        assertEquals(DeviceState.OFF, runtime.state());
        assertEquals(DeviceState.OFF, runtime.rawState());
    }

    @Test
    void inputRuntime_createdWithState_initializesCorrectly() {
        InputDevice dev = new InputDevice("motion_1", "Motion 1", InputDevice.SensorType.MOTION, false);
        InputRuntime runtime = new InputRuntime(dev, DeviceState.ON);

        assertEquals(DeviceState.ON, runtime.state());
        assertEquals(DeviceState.ON, runtime.rawState());
    }

    @Test
    void setRawState_updatesRawState() {
        InputDevice dev = new InputDevice("contact_1", "Contact 1", InputDevice.SensorType.CONTACT, false);
        InputRuntime runtime = new InputRuntime(dev);

        runtime.setRawState(DeviceState.ON);
        assertEquals(DeviceState.ON, runtime.rawState());
        assertEquals(DeviceState.ON, runtime.state());
    }

    @Test
    void state_appliesInversion_onInvertedDevice() {
        InputDevice dev = new InputDevice("contact_1", "Contact 1", InputDevice.SensorType.CONTACT, true);
        InputRuntime runtime = new InputRuntime(dev, DeviceState.ON);

        // Raw ON on inverted device → logical OFF
        assertEquals(DeviceState.OFF, runtime.state());

        runtime.setRawState(DeviceState.OFF);
        // Raw OFF on inverted device → logical ON
        assertEquals(DeviceState.ON, runtime.state());
    }

    @Test
    void state_noInversion_passesThroughRawState() {
        InputDevice dev = new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false);
        InputRuntime runtime = new InputRuntime(dev, DeviceState.ON);

        assertEquals(DeviceState.ON, runtime.state());

        runtime.setRawState(DeviceState.OFF);
        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void config_returnsUnderlyingInputDevice() {
        InputDevice dev = new InputDevice("door_1", "Door 1", InputDevice.SensorType.DOOR, false);
        InputRuntime runtime = new InputRuntime(dev);

        assertSame(dev, runtime.config());
        assertEquals("door_1", runtime.id());
        assertEquals(InputDevice.SensorType.DOOR, runtime.config().sensorType());
    }

    @Test
    void rawStateAndLogicalState_differOnInverted() {
        InputDevice dev = new InputDevice("c1", "C1", InputDevice.SensorType.CONTACT, true);
        InputRuntime runtime = new InputRuntime(dev, DeviceState.ON);

        assertEquals(DeviceState.ON, runtime.rawState());
        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void rawStateAndLogicalState_sameOnNonInverted() {
        InputDevice dev = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        InputRuntime runtime = new InputRuntime(dev, DeviceState.ON);

        assertEquals(DeviceState.ON, runtime.rawState());
        assertEquals(DeviceState.ON, runtime.state());
    }
}
