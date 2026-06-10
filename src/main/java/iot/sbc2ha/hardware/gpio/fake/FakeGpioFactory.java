package iot.sbc2ha.hardware.gpio.fake;

import iot.sbc2ha.hardware.gpio.GpioFactory;
import iot.sbc2ha.hardware.gpio.GpioInputAdapter;
import iot.sbc2ha.hardware.gpio.GpioOutputAdapter;

/**
 * A {@link GpioFactory} that creates fake GPIO adapters.
 *
 * <p>Provides a singleton instance for use in tests that need a hardware
 * factory but must not depend on diozero or real hardware.</p>
 */
public final class FakeGpioFactory implements GpioFactory {

    public static final FakeGpioFactory INSTANCE = new FakeGpioFactory();

    private FakeGpioFactory() {}

    @Override
    public GpioInputAdapter createInput(String pinId) {
        return new FakeGpioInputAdapter();
    }

    @Override
    public GpioOutputAdapter createOutput(String pinId) {
        return new FakeGpioOutputAdapter();
    }
}
