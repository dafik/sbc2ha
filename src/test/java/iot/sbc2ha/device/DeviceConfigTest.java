package iot.sbc2ha.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceConfigTest {

    @Test
    void switchDevice_createsWithAllFields() {
        SwitchDevice switch1 = new SwitchDevice("switch_1", "switch 1", "light_1");
        assertEquals("switch_1", switch1.id());
        assertEquals("switch 1", switch1.displayName());
        assertEquals("light_1", switch1.clickAction());
        assertEquals(DeviceConfig.DeviceType.SWITCH, switch1.type());
    }

    @Test
    void switchDevice_clickActionNullByDefault() {
        SwitchDevice switch1 = new SwitchDevice("switch_1", "switch 1", null);
        assertNull(switch1.clickAction());
    }

    @Test
    void lightDevice_createsWithFields() {
        LightDevice light = new LightDevice("light_1", "Kitchen");
        assertEquals("light_1", light.id());
        assertEquals("Kitchen", light.displayName());
        assertEquals(DeviceConfig.DeviceType.LIGHT, light.type());
    }

    @Test
    void outputDevice_createsWithFields() {
        OutputDevice out = new OutputDevice("out_1", "Relay 1");
        assertEquals("out_1", out.id());
        assertEquals("Relay 1", out.displayName());
        assertEquals(DeviceConfig.DeviceType.OUTPUT, out.type());
    }

    @Test
    void switchDevice_equalsAndHashCode() {
        SwitchDevice a = new SwitchDevice("switch_1", "B1", "light_1");
        SwitchDevice b = new SwitchDevice("switch_1", "B1", "light_1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void switchDevice_notEqual_differentClickAction() {
        SwitchDevice a = new SwitchDevice("switch_1", "B1", "light_1");
        SwitchDevice b = new SwitchDevice("switch_1", "B1", "light_2");
        assertNotEquals(a, b);
    }

    @Test
    void lightDevice_equalsAndHashCode() {
        LightDevice a = new LightDevice("light_1", "L1");
        LightDevice b = new LightDevice("light_1", "L1");
        assertEquals(a, b);
    }

    @Test
    void outputDevice_equalsAndHashCode() {
        OutputDevice a = new OutputDevice("out_1", "O1");
        OutputDevice b = new OutputDevice("out_1", "O1");
        assertEquals(a, b);
    }
}
