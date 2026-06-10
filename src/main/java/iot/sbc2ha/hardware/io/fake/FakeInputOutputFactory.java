package iot.sbc2ha.hardware.io.fake;

import iot.sbc2ha.hardware.io.InputOutputFactory;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.OutputAdapter;

/**
 * A {@link InputOutputFactory} that creates fake GPIO adapters.
 *
 * <p>Provides a singleton instance for use in tests that need a hardware
 * factory but must not depend on diozero or real hardware.</p>
 */
public final class FakeInputOutputFactory implements InputOutputFactory {

    public static final FakeInputOutputFactory INSTANCE = new FakeInputOutputFactory();

    private FakeInputOutputFactory() {}

    @Override
    public InputAdapter createInput(String pinId) {
        return new FakeInputAdapter();
    }

    @Override
    public OutputAdapter createOutput(String pinId) {
        return new FakeOutputAdapter();
    }
}
