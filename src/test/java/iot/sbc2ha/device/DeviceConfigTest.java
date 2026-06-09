package iot.sbc2ha.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceConfigTest {

    @Test
    void buttonDevice_createsWithAllFields() {
        ButtonDevice btn = new ButtonDevice("btn_1", "Btn 1", "light_1");
        assertEquals("btn_1", btn.id());
        assertEquals("Btn 1", btn.displayName());
        assertEquals("light_1", btn.clickAction());
        assertEquals(DeviceConfig.DeviceType.BUTTON, btn.type());
    }

    @Test
    void buttonDevice_clickActionNullByDefault() {
        ButtonDevice btn = new ButtonDevice("btn_1", "Btn 1", null);
        assertNull(btn.clickAction());
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
    void buttonDevice_equalsAndHashCode() {
        ButtonDevice a = new ButtonDevice("btn_1", "B1", "light_1");
        ButtonDevice b = new ButtonDevice("btn_1", "B1", "light_1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void buttonDevice_notEqual_differentClickAction() {
        ButtonDevice a = new ButtonDevice("btn_1", "B1", "light_1");
        ButtonDevice b = new ButtonDevice("btn_1", "B1", "light_2");
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
