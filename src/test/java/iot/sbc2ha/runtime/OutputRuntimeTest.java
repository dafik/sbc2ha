package iot.sbc2ha.runtime;

import iot.sbc2ha.device.OutputDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OutputRuntime}.
 */
class OutputRuntimeTest {

    @Test
    void outputCreatedDefault_startsOff() {
        OutputDevice dev = new OutputDevice("out_1", "Relay 1");
        OutputRuntime runtime = new OutputRuntime(dev);

        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void outputCreatedWithState_initializesCorrectly() {
        OutputDevice dev = new OutputDevice("out_2", "Relay 2");
        OutputRuntime runtime = new OutputRuntime(dev, DeviceState.ON);

        assertEquals(DeviceState.ON, runtime.state());
    }

    @Test
    void toggle_offToOn() {
        OutputDevice dev = new OutputDevice("out_3", "Relay 3");
        OutputRuntime runtime = new OutputRuntime(dev);

        DeviceState newState = runtime.toggle();

        assertEquals(DeviceState.ON, newState);
        assertEquals(DeviceState.ON, runtime.state());
    }

    @Test
    void toggle_onToOff() {
        OutputDevice dev = new OutputDevice("out_4", "Relay 4");
        OutputRuntime runtime = new OutputRuntime(dev, DeviceState.ON);

        DeviceState newState = runtime.toggle();

        assertEquals(DeviceState.OFF, newState);
        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void setState_updatesDirectly() {
        OutputDevice dev = new OutputDevice("out_5", "Relay 5");
        OutputRuntime runtime = new OutputRuntime(dev);

        runtime.setState(DeviceState.ON);
        assertEquals(DeviceState.ON, runtime.state());

        runtime.setState(DeviceState.OFF);
        assertEquals(DeviceState.OFF, runtime.state());
    }

    @Test
    void config_returnsUnderlyingDevice() {
        OutputDevice dev = new OutputDevice("out_6", "Relay 6");
        OutputRuntime runtime = new OutputRuntime(dev);

        assertSame(dev, runtime.config());
        assertEquals("out_6", runtime.id());
    }
}
