package iot.sbc2ha.device;

import iot.sbc2ha.config.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceRegistryTest {

    @Test
    void acceptsUniqueDevices() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_1", "switch 1", null));
        reg.add(new LightDevice("light_1", "Light 1"));
        reg.add(new OutputDevice("out_1", "Out 1"));
        assertDoesNotThrow(reg::validate);
        assertEquals(3, reg.size());
    }

    @Test
    void rejectsDuplicateId() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_1", "switch 1", null));
        reg.add(new SwitchDevice("switch_1", "switch 2", null));
        ValidationException ex = assertThrows(ValidationException.class, reg::validate);
        assertTrue(ex.getMessage().contains("Duplicate"));
    }

    @Test
    void acceptsValidClickActionTarget() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new LightDevice("light_kitchen", "Kitchen light"));
        reg.add(new SwitchDevice("switch_entrance", "Entrance", "light_kitchen"));
        assertDoesNotThrow(reg::validate);
    }

    @Test
    void rejectsUnknownClickActionTarget() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_entrance", "Entrance", "light_missing"));
        ValidationException ex = assertThrows(ValidationException.class, reg::validate);
        assertTrue(ex.getMessage().contains("unknown device"));
    }

    @Test
    void acceptsNullClickAction() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_free", "Free switch", null));
        assertDoesNotThrow(reg::validate);
    }

    @Test
    void acceptsEmptyClickAction() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_free", "Free switch", ""));
        assertDoesNotThrow(reg::validate);
    }

    @Test
    void switchs_returnsOnlySwitchs() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new SwitchDevice("switch_1", "B1", null));
        reg.add(new LightDevice("light_1", "L1"));
        reg.add(new SwitchDevice("switch_2", "B2", "light_1"));
        assertEquals(2, reg.switchs().size());
    }

    @Test
    void lights_returnsOnlyLights() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new LightDevice("light_1", "L1"));
        reg.add(new OutputDevice("out_1", "O1"));
        assertEquals(1, reg.lights().size());
        assertInstanceOf(LightDevice.class, reg.lights().getFirst());
    }

    @Test
    void outputs_returnsOnlyOutputs() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new OutputDevice("out_1", "O1"));
        assertEquals(1, reg.outputs().size());
        assertInstanceOf(OutputDevice.class, reg.outputs().getFirst());
    }

    @Test
    void getById_returnsCorrectDevice() {
        DeviceRegistry reg = new DeviceRegistry();
        reg.add(new LightDevice("light_1", "L1"));
        DeviceConfig found = reg.getById("light_1");
        assertInstanceOf(LightDevice.class, found);
    }

    @Test
    void getById_returnsNullForMissing() {
        DeviceRegistry reg = new DeviceRegistry();
        assertNull(reg.getById("nonexistent"));
    }
}
