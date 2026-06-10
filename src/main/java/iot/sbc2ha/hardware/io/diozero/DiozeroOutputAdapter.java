package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.I2CDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.sbc.DeviceFactoryHelper;
import iot.sbc2ha.hardware.io.OutputAdapter;
import iot.sbc2ha.runtime.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diozero-backed output adapter for MCP23017 I2C GPIO expander chips.
 *
 * <p>Uses I2C register writes to control MCP23017 output latches (OLAT).
 * Reads OLAT back to verify pin state. Supports inversion (active-low wiring).</p>
 *
 * <h3>Pin location format</h3>
 * <p>The {@code pinId} parameter encodes the I2C location of the pin on
 * the MCP23017 chip:</p>
 * <pre>
 * i2c-bus : address : port : pin
 * i2c-1   : 0x20  : A    : 0
 * </pre>
 * <p>Port is {@code "A"} or {@code "B"}, pin is 0-7. The I2C address
 * supports both hex ({@code 0x20}) and decimal ({@code 32}) formats.</p>
 *
 * <h3>MCP23017 registers used</h3>
 * <table>
 *   <tr><td>OLATA (0x0A)</td><td>Output Latch Port A — write to set, read to query</td></tr>
 *   <tr><td>OLATB (0x0B)</td><td>Output Latch Port B</td></tr>
 * </table>
 *
 * <p>This class is the boundary where diozero I2C types enter the system.
 * All other packages must interact through {@link OutputAdapter}.</p>
 *
 * @see OutputAdapter
 */
public final class DiozeroOutputAdapter implements OutputAdapter {

    private static final Logger Log = LoggerFactory.getLogger(DiozeroOutputAdapter.class);

    // MCP23017 register addresses (bank mode)
    private static final int OLAT_A = 0x0A;
    private static final int OLAT_B = 0x0B;

    private final int i2cBus;
    private final int i2cAddress;
    private final int pin;       // global pin number (0-15): portA=0..7, portB=8..15
    private final boolean inverted;

    // Cached logical state — updated on every write() and read()
    private DeviceState state = DeviceState.OFF;

    /**
     * Create an MCP23017 output adapter from a location string.
     *
     * @param pinId    the I2C location string (e.g. "i2c-1:0x20:A:0")
     * @param inverted whether logical ON maps to physical LOW
     */
    @SuppressWarnings("unused")
    public DiozeroOutputAdapter(String pinId, boolean inverted) {
        this.inverted = inverted;

        String[] parts = pinId.split(":");
        if (parts.length < 4) {
            throw new IllegalArgumentException(
                    "Invalid MCP23017 pin location: '" + pinId
                    + "' (expected 'i2c-bus:addr:port:pin')");
        }

        this.i2cBus = Integer.parseInt(parts[0].replace("i2c-", ""));
        this.i2cAddress = parseAddress(parts[1]);
        String portStr = parts[2];
        int portPin = Integer.parseInt(parts[3]);

        if (!"A".equals(portStr) && !"B".equals(portStr)) {
            throw new IllegalArgumentException(
                    "Invalid MCP23017 port: '" + portStr + "' (expected 'A' or 'B')");
        }

        if (portPin < 0 || portPin > 7) {
            throw new IllegalArgumentException(
                    "Invalid MCP23017 pin: " + portPin + " (expected 0-7)");
        }

        this.pin = ("A".equals(portStr) ? 0 : 8) + portPin;

        // Ensure native device factory is initialised (triggers provider load)
        DeviceFactoryHelper.getNativeDeviceFactory();

        // Establish baseline: read OLAT register from the chip
        try {
            int olatByte = readOlat(i2cBus, i2cAddress, pin);
            state = ((olatByte & (1 << (pin & 0x07))) != 0)
                    ? DeviceState.ON : DeviceState.OFF;
        } catch (RuntimeIOException e) {
            Log.warn("Could not read initial OLAT state (bus={}, addr=0x{}): {}",
                    i2cBus, String.format("%02X", i2cAddress), e.getMessage());
            state = DeviceState.OFF;
        }

        Log.info("MCP23017 output adapter created: bus={}, addr=0x{}, pin={}, inverted={}",
                i2cBus, String.format("%02X", i2cAddress), pin, inverted);
    }

    // -----------------------------------------------------------------------
    // Package-private constructor for factory use (avoids string parsing)
    // -----------------------------------------------------------------------

    /**
     * Create an adapter with pre-parsed I2C address and pin number.
     * <p>Skips hardware read — starts with default OFF state.
     * Used by tests to avoid I2C dependencies.</p>
     */
    DiozeroOutputAdapter(int i2cBus, int i2cAddress, int pin, boolean inverted) {
        this.i2cBus = i2cBus;
        this.i2cAddress = i2cAddress;
        this.pin = pin;
        this.inverted = inverted;
        // Skip hardware read — tests verify logical state via write/read()
    }

    // -----------------------------------------------------------------------
    // Static utilities
    // -----------------------------------------------------------------------

    private static int parseAddress(String addrStr) {
        if (addrStr.startsWith("0x") || addrStr.startsWith("0X")) {
            return Integer.parseUnsignedInt(addrStr.substring(2), 16);
        }
        return Integer.parseInt(addrStr);
    }

    /**
     * Read an OLAT register byte from the MCP23017 chip using diozero I2C API.
     *
     * @param bus       I2C bus number
     * @param address   MCP23017 I2C address
     * @param pin       pin number (0-15) — determines port (A=0..7, B=8..15)
     * @return the OLAT byte for the relevant port
     * @throws RuntimeIOException on I2C communication failure
     */
    static int readOlat(int bus, int address, int pin) throws RuntimeIOException {
        int reg = (pin < 8) ? OLAT_A : OLAT_B;
        try (I2CDevice i2c = new I2CDevice(bus, address)) {
            return i2c.readByteData(reg);
        }
    }

    /**
     * Write a bit value to the OLAT register of an MCP23017 chip.
     *
     * @param bus       I2C bus number
     * @param address   MCP23017 I2C address
     * @param pin       pin number (0-15)
     * @param bitValue  the bit value to write (0 or 1 << pin_bit)
     * @throws RuntimeIOException on I2C communication failure
     */
    static void writeOlat(int bus, int address, int pin, byte bitValue) throws RuntimeIOException {
        int reg = (pin < 8) ? OLAT_A : OLAT_B;
        try (I2CDevice i2c = new I2CDevice(bus, address)) {
            i2c.writeByteData(reg, bitValue);
        }
    }

    // -----------------------------------------------------------------------
    // OutputAdapter
    // -----------------------------------------------------------------------

    @Override
    public void write(DeviceState desiredState) {
        // Map logical state to physical (respecting inversion)
        DeviceState physicalState = inverted
                ? (desiredState == DeviceState.ON ? DeviceState.OFF : DeviceState.ON)
                : desiredState;

        byte bitValue = (byte) (physicalState == DeviceState.ON
                ? (1 << (pin & 0x07)) : 0);

        synchronized (this) {
            try {
                writeOlat(i2cBus, i2cAddress, pin, bitValue);
                state = desiredState;
            } catch (RuntimeException e) {
                Log.error("EXACTLY FAILED to write MCP23017 pin {} (bus={}, addr=0x{}): {} - class: {}",
                        pin, i2cBus, String.format("%02X", i2cAddress), e.getClass().getName(), e.getMessage());
                e.printStackTrace(System.err);
                // State not updated on I2C error — stale state preserved
            }
        }
    }

    @Override
    public DeviceState read() {
        synchronized (this) {
            try {
                int olatByte = readOlat(i2cBus, i2cAddress, pin);
                boolean raw = (olatByte & (1 << (pin & 0x07))) != 0;
                state = (inverted != raw) ? DeviceState.ON : DeviceState.OFF;
            } catch (RuntimeIOException e) {
                Log.warn("Failed to read MCP23017 pin {} (bus={}, addr=0x{}): {}",
                        pin, i2cBus, String.format("%02X", i2cAddress), e.getMessage());
                // state unchanged — stale state returned
            }
        }
        return state;
    }

    @Override
    public void close() {
        Log.info("Closed MCP23017 output adapter (bus={}, addr=0x{}, pin={})",
                i2cBus, String.format("%02X", i2cAddress), pin);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /**
     * Current logical state of this output pin.
     */
    public DeviceState getState() {
        return state;
    }

    /**
     * I2C bus number this adapter targets.
     */
    public int i2cBus() {
        return i2cBus;
    }

    /**
     * I2C device address.
     */
    public int i2cAddress() {
        return i2cAddress;
    }

    /**
     * Global pin number (0-15) on the MCP23017 chip.
     */
    public int pin() {
        return pin;
    }

    @Override
    public String toString() {
        return "DiozeroOutputAdapter{bus=" + i2cBus + ", addr=0x"
                + Integer.toHexString(i2cAddress) + ", pin=" + pin + "}";
    }
}
