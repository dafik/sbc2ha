package iot.sbc2ha;

import iot.sbc2ha.config.Sbc2haConfig;
import iot.sbc2ha.device.SwitchDevice;
import iot.sbc2ha.device.OutputDevice;
import iot.sbc2ha.runtime.ActionEngine;
import iot.sbc2ha.runtime.SwitchRuntime;
import iot.sbc2ha.runtime.DeviceRuntime;
import iot.sbc2ha.runtime.DeviceState;
import iot.sbc2ha.runtime.OutputRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: fake switch click toggles fake output.
 *
 * <p>Verifies the complete fake runtime pipeline:
 * config → device registry → action engine → click dispatch → output toggle.</p>
 */
class MainTest {

    /**
     * Fake input click toggles fake output.
     */
    @Test
    void testFakeToggleFlow() {
        // 1. Build config manually (no YAML I/O needed for this test)
        Sbc2haConfig config = new Sbc2haConfig("test_node", "1");

        OutputDevice output = new OutputDevice("out_relay1", "Test Relay");
        SwitchDevice switch1 = new SwitchDevice("switch_entrance", "Entrance Switch", "out_relay1");

        config.setDevices(java.util.List.of(switch1, output));
        config.validate();

        // 2. Wire action engine (creates runtimes, resolves targets)
        ActionEngine engine = new ActionEngine(config.registry());

        // 3. Verify initial state
        DeviceRuntime target = engine.getTarget("out_relay1");
        assertNotNull(target, "Output target must exist in engine");
        assertInstanceOf(OutputRuntime.class, target);
        OutputRuntime outputRuntime = (OutputRuntime) target;
        assertEquals(DeviceState.OFF, outputRuntime.state(),
                "Output must start in OFF state");

        // 4. Simulate switch click → dispatch to action engine
        SwitchRuntime switchRuntime = engine.getSwitch("switch_entrance");
        assertNotNull(switchRuntime, "Switch runtime must exist in engine");
        engine.dispatchClick(switchRuntime);

        // 5. Verify output toggled to ON
        assertEquals(DeviceState.ON, outputRuntime.state(),
                "Output must toggle to ON after first click");

        // 6. Second click → toggle back to OFF
        engine.dispatchClick(switchRuntime);
        assertEquals(DeviceState.OFF, outputRuntime.state(),
                "Output must toggle back to OFF after second click");
    }
}
