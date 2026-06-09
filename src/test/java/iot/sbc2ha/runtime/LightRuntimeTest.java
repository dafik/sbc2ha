package iot.sbc2ha.runtime;

import iot.sbc2ha.device.LightDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LightRuntime}.
 */
class LightRuntimeTest {

    @Test
    void lightCreatedDefault_startsOff() {
        LightDevice dev = new LightDevice("light_1", "Kitchen Light");
        LightRuntime runtime = new LightRuntime(dev);

        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void toggle_offToOn() {
        LightDevice dev = new LightDevice("light_2", "Bedroom Light");
        LightRuntime runtime = new LightRuntime(dev);

        DeviceState newState = runtime.toggle();

        assertEquals(DeviceState.ON, newState);
        assertEquals(DeviceState.ON, runtime.state());
    }

    @Test
    void toggle_onToOff() {
        LightDevice dev = new LightDevice("light_3", "Garden Light");
        LightRuntime runtime = new LightRuntime(dev, DeviceState.ON);

        DeviceState newState = runtime.toggle();

        assertEquals(DeviceState.OFF, newState);
        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void config_returnsUnderlyingDevice() {
        LightDevice dev = new LightDevice("light_4", "Hall Light");
        LightRuntime runtime = new LightRuntime(dev);

        assertSame(dev, runtime.config());
        assertEquals("light_4", runtime.id());
    }
}
