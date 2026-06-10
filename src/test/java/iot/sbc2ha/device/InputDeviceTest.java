package iot.sbc2ha.device;

import iot.sbc2ha.runtime.DeviceState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputDeviceTest {

    @Test
    void inputDevice_createsWithAllFields() {
        InputDevice door = new InputDevice("door_entrance", "Entrance door", InputDevice.SensorType.DOOR, false);
        assertEquals("door_entrance", door.id());
        assertEquals("Entrance door", door.displayName());
        assertEquals(InputDevice.SensorType.DOOR, door.sensorType());
        assertFalse(door.inverted());
        assertEquals(DeviceConfig.DeviceType.INPUT, door.type());
    }

    @Test
    void inputDevice_defaultsInvertedToFalse() {
        InputDevice motion = new InputDevice("motion_hall", "Hall motion", InputDevice.SensorType.MOTION);
        assertFalse(motion.inverted());
    }

    @Test
    void inputDevice_inverted_true() {
        InputDevice contact = new InputDevice("contact_window", "Window contact", InputDevice.SensorType.CONTACT, true);
        assertTrue(contact.inverted());
    }

    @Test
    void sensorType_door() {
        InputDevice dev = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR);
        assertEquals(InputDevice.SensorType.DOOR, dev.sensorType());
    }

    @Test
    void sensorType_motion() {
        InputDevice dev = new InputDevice("m1", "M1", InputDevice.SensorType.MOTION);
        assertEquals(InputDevice.SensorType.MOTION, dev.sensorType());
    }

    @Test
    void sensorType_contact() {
        InputDevice dev = new InputDevice("c1", "C1", InputDevice.SensorType.CONTACT);
        assertEquals(InputDevice.SensorType.CONTACT, dev.sensorType());
    }

    @Test
    void logicalState_noInversion_rawOn_givesOn() {
        InputDevice door = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        assertEquals(DeviceState.ON, door.logicalState(DeviceState.ON));
    }

    @Test
    void logicalState_noInversion_rawOff_givesOff() {
        InputDevice door = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        assertEquals(DeviceState.OFF, door.logicalState(DeviceState.OFF));
    }

    @Test
    void logicalState_inverted_rawOn_givesOff() {
        InputDevice contact = new InputDevice("c1", "C1", InputDevice.SensorType.CONTACT, true);
        assertEquals(DeviceState.OFF, contact.logicalState(DeviceState.ON));
    }

    @Test
    void logicalState_inverted_rawOff_givesOn() {
        InputDevice contact = new InputDevice("c1", "C1", InputDevice.SensorType.CONTACT, true);
        assertEquals(DeviceState.ON, contact.logicalState(DeviceState.OFF));
    }

    @Test
    void inputDevice_equalsAndHashCode() {
        InputDevice a = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        InputDevice b = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void inputDevice_notEqual_differentSensorType() {
        InputDevice a = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        InputDevice b = new InputDevice("d1", "D1", InputDevice.SensorType.MOTION, false);
        assertNotEquals(a, b);
    }

    @Test
    void inputDevice_notEqual_differentInverted() {
        InputDevice a = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, false);
        InputDevice b = new InputDevice("d1", "D1", InputDevice.SensorType.DOOR, true);
        assertNotEquals(a, b);
    }
}
