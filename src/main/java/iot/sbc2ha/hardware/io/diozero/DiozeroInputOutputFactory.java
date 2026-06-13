package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;
import com.diozero.devices.mcp23xxx.MCP23xxx;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import iot.sbc2ha.boot.BootDisplay;
import iot.sbc2ha.boot.OledBootDisplay;
import iot.sbc2ha.hardware.GpioChannel;
import iot.sbc2ha.hardware.HardwareChip;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.Mcp23017Channel;
import iot.sbc2ha.hardware.OledChannel;
import iot.sbc2ha.hardware.PhysicalChannel;
import iot.sbc2ha.hardware.io.InputOutputFactory;
import iot.sbc2ha.hardware.io.InputAdapter;
import iot.sbc2ha.hardware.io.OutputAdapter;
import iot.sbc2ha.hardware.io.OutputDelegate;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diozero-backed implementation of {@link InputOutputFactory}.
 *
 * <p>Creates real hardware adapters that drive GPIO pins.
 * The actual hardware platform is determined by the diozero provider
 * on the classpath (e.g. BBBioLib, RPi).</p>
 *
 * <ul>
 *   <li><b>Input</b> — direct BBB GPIO via {@link DiozeroInputAdapter}</li>
 *   <li><b>Output</b> — dispatches by {@link PhysicalChannel#channelType()} to create
 *      MCP23017, PCA9685, or GPIO adapters</li>
 * </ul>
 *
 * <h3>Singleton</h3>
 * <p>Use {@link #INSTANCE} to obtain the singleton factory rather than
 * constructing new instances — this avoids repeated native GPIO
 * initialisation overhead.</p>
 */
@SuppressWarnings("unused")
public final class DiozeroInputOutputFactory implements InputOutputFactory {

    private static final Logger Log = LoggerFactory.getLogger(DiozeroInputOutputFactory.class);

    @SuppressWarnings("unused")
    public static final DiozeroInputOutputFactory INSTANCE = new DiozeroInputOutputFactory();

    private DiozeroInputOutputFactory() {
        Log.info("Diozero GPIO factory initialised");
    }

    @Override
    public InputAdapter createInput(String pinId) {
        Log.debug("Creating Diozero GPIO input adapter for pin '{}'", pinId);
        return new DiozeroInputAdapter(pinId, false);
    }

    /**
     * Create an input adapter with chip resolution from the given hardware model.
     *
     * @param channel the physical channel
     * @param model   the hardware model containing chip declarations (may be null)
     * @return the input adapter
     */
    public InputAdapter createInput(PhysicalChannel channel, HardwareModel model) {
        return switch (channel.channelType()) {
            case MCP23017 -> createMcp23017Input((Mcp23017Channel) channel, model);
            case GPIO -> createGpioInput((GpioChannel) channel);
            default -> throw new IllegalArgumentException(
                    "Unsupported channel type for input: " + channel.channelType());
        };
    }

    private InputAdapter createMcp23017Input(Mcp23017Channel channel, HardwareModel model) {
        // TODO: MCP23017 input support — deferred to next iteration
        throw new UnsupportedOperationException(
                "MCP23017 input not yet supported (pin=" + channel.pin() + ")");
    }

    private InputAdapter createGpioInput(GpioChannel channel) {
        return new DiozeroInputAdapter(channel.pinLabel(), false);
    }

    @Override
    public OutputAdapter createOutput(PhysicalChannel channel) {
        Log.debug("Creating output adapter for channel: {}", channel);
        return switch (channel.channelType()) {
            case MCP23017 -> createMcp23017Output((Mcp23017Channel) channel);
            case GPIO -> createGpioOutput((GpioChannel) channel);
            default -> throw new IllegalArgumentException(
                    "Unsupported channel type: " + channel.channelType());
        };
    }

    /**
     * Create an output adapter with chip resolution from the given hardware model.
     *
     * @param channel   the physical channel
     * @param model     the hardware model containing chip declarations
     * @return the output adapter
     */
    public OutputAdapter createOutput(PhysicalChannel channel, HardwareModel model) {
        Log.debug("Creating output adapter for channel: {} with model: {}", channel, model);
        return switch (channel.channelType()) {
            case MCP23017 -> createMcp23017Output((Mcp23017Channel) channel, model);
            case GPIO -> createGpioOutput((GpioChannel) channel);
            default -> throw new IllegalArgumentException(
                    "Unsupported channel type: " + channel.channelType());
        };
    }

    private OutputAdapter createMcp23017Output(Mcp23017Channel channel) {
        return createMcp23017Output(channel, null);
    }

    private OutputAdapter createMcp23017Output(Mcp23017Channel channel, HardwareModel model) {
        String busId = channel.bus();
        int i2cBus, i2cAddress;

        if (model != null && busId != null) {
            HardwareChip chip = model.getChip(busId);
            if (chip == null) {
                throw new IllegalArgumentException(
                        "Unknown chip id '" + busId + "' for MCP23017 channel pin=" + channel.pin());
            }
            i2cBus = chip.i2cBus();
            i2cAddress = chip.i2cAddress();
        } else {
            // Fallback: no model provided, use defaults
            i2cBus = 2;
            i2cAddress = 0x20;
            Log.warn("No HardwareModel provided for MCP23017 channel — using default bus={}, addr=0x20",
                    i2cBus);
        }

        int globalPin = channel.pin();

        HardwareComponentKey chipKey = new HardwareComponentKey(
                "mcp23017", i2cBus + ":" + i2cAddress);
        MCP23017 mcp = HardwareComponentRegistry.INSTANCE.getOrRegister(chipKey, () -> {
            try {
                Log.info("Creating MCP23017: bus={}, addr=0x{:02X}", i2cBus, i2cAddress);
                return new MCP23017(i2cBus, i2cAddress, MCP23xxx.INTERRUPT_GPIO_NOT_SET);
            } catch (Exception e) {
                Log.error("Failed to create MCP23017: {}", e.getMessage());
                return null;
            }
        });

        OutputDelegate delegate = mcp != null
                ? OutputDelegateFactory.create(mcp, globalPin)
                : createNoopDelegate(chipKey, globalPin);

        return new DiozeroOutputAdapter(false, delegate);
    }

    private OutputAdapter createGpioOutput(GpioChannel channel) {
        String pinId = channel.pinLabel();
        DeviceFactoryHelper.getNativeDeviceFactory();
        BoardInfo board = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
        PinInfo pinInfo = DiozeroInputAdapter.resolvePin(board, pinId);
        if (pinInfo == null) {
            throw new IllegalArgumentException("Unknown GPIO output pin: " + pinId);
        }
        PinInfo resolved = PinModeOverrides.wrap(pinInfo,
                PinModeOverrides.loadModes(""), PinModeOverrides.loadChipOverrides(""));
        Log.info("Creating GPIO output adapter for pin '{}' (sysfs={}, chip={})",
                pinId, resolved.getSysFsNumber(), resolved.getChip());
        OutputDelegate delegate = OutputDelegateFactory.create(resolved, pinId);
        return new DiozeroOutputAdapter(false, delegate);
    }

    /**
     * Create a no-op delegate for when the MCP23017 chip could not be created.
     */
    private static OutputDelegate createNoopDelegate(HardwareComponentKey chipKey, int globalPin) {
        Log.warn("MCP23017 not created for key {} — returning non-functional delegate", chipKey);
        return new OutputDelegate() {
            @Override
            public void write(DeviceState state) {}
            @Override
            public DeviceState read() { return DeviceState.OFF; }
            @Override
            public void close() {}
        };
    }

    /**
     * Create an OLED boot display from the given channel definition.
     *
     * <p>Fail-open: returns {@code null} if the display cannot be initialised
     * (e.g. I2C bus unavailable, device not responding).</p>
     *
     * @param channel the OLED channel definition
     * @param model   the hardware model containing chip declarations (may be null)
     * @return a new {@link BootDisplay}, or {@code null} on failure
     */
    public BootDisplay createOledDisplay(OledChannel channel, HardwareModel model) {
        try {
            int i2cBus, i2cAddress;
            if (model != null && channel.bus() != null) {
                HardwareChip chip = model.getChip(channel.bus());
                if (chip == null) {
                    throw new IllegalArgumentException(
                            "Unknown chip id '" + channel.bus() + "' for OLED channel");
                }
                i2cBus = chip.i2cBus();
                i2cAddress = chip.i2cAddress();
            } else {
                i2cBus = 2;
                i2cAddress = 0x3C;
                Log.warn("No HardwareModel provided for OLED channel — using default bus={}, addr=0x3C",
                        i2cBus);
            }
            Log.info("Creating OLED display: bus={}, addr=0x{:02X}", i2cBus, i2cAddress);
            return new OledBootDisplay(i2cBus, i2cAddress);
        } catch (Exception e) {
            Log.warn("OLED display creation failed (non-fatal): {}", e.getMessage());
            return null;
        }
    }
}
