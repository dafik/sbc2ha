package iot.sbc2ha.hardware.io.diozero;

import com.diozero.api.DeviceMode;
import com.diozero.api.PinInfo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * A {@link PinInfo} that layers override modes, chip numbers, and line offsets
 * on top of a base pin.
 *
 * <p>Diozero's board definitions sometimes have empty mode lists for BBB pins
 * (e.g. LCD cape pins P8_37–P8_46) and incorrect gpiochip numbers / line
 * offsets for kernel 6.x (written for kernel 4.x + cape-universal).  This
 * class extends {@code PinInfo} so it can be passed directly to any diozero
 * Builder, while adding:</p>
 * <ul>
 *   <li>{@code DIGITAL_INPUT} support for pins listed in the override config</li>
 *   <li>Correct gpiochip numbers for the running kernel version</li>
 *   <li>Correct line offsets for the running kernel version (kernel 6.x)</li>
 * </ul>
 *
 * <p>Delegates all attributes to the wrapped {@code PinInfo} except
 * {@code isSupported()}, {@code getModes()}, {@code getChip()}, and
 * {@code getLineOffset()}, which may include overrides.</p>
 */
final class PinInfoView extends PinInfo {

    private final PinInfo delegate;
    private final Set<DeviceMode> overrides;

    /**
     * Create a view that layers {@code overrides}, {@code chipOverride},
     * and {@code lineOffsetOverride} on top of {@code delegate}.
     *
     * <p>All PinInfo attributes (header, etc.) come from
     * {@code delegate}; only mode checks, chip number, and line offset
     * may be augmented.</p>
     *
     * @param delegate           the base PinInfo resolved from diozero board definitions
     * @param overrides          set of additional DeviceModes (may be null)
     * @param chipOverride       new chip id for gpiochip, or null to use delegate's value
     * @param lineOffsetOverride new line offset, or null to use delegate's value
     */
    PinInfoView(PinInfo delegate, Set<DeviceMode> overrides, Integer chipOverride,
                Integer lineOffsetOverride) {
        // Resolve the effective chip and line to pass to super()
        int effectiveChip = chipOverride != null ? chipOverride : delegate.getChip();
        int effectiveLine = lineOffsetOverride != null ? lineOffsetOverride : delegate.getLineOffset();

        // Compute sysfs from chip+line when we have an override.
        // The kernel assigns global GPIO numbers as chip*32 + line:
        //   gpiochip0 → gpio-0..31, gpiochip1 → gpio-32..63, etc.
        // When the delegate has sysfs=-1 (pin not in diozero board defs),
        // the override chip/line gives us the kernel mapping.
        int sysfs;
        if (chipOverride != null && lineOffsetOverride != null
                && delegate.getSysFsNumber() == -1) {
            sysfs = effectiveChip * 32 + effectiveLine;
        } else {
            sysfs = delegate.getSysFsNumber();
        }

        // Compute the modes to pass to super(): combine original modes with
        // overrides so the parent PinInfo's internal modes list is never empty
        // when overrides exist (diozero's Builder validates getModes()).
        Collection<DeviceMode> modesForSuper;
        if (overrides != null && !overrides.isEmpty()) {
            EnumSet<DeviceMode> merged = EnumSet.copyOf(overrides);
            merged.addAll(delegate.getModes());
            modesForSuper = merged;
        } else {
            modesForSuper = delegate.getModes();
        }

        super(delegate.getKeyPrefix(),
              delegate.getHeader(),
              delegate.getDeviceNumber(),
              delegate.getPhysicalPin(),
              delegate.getName(),
              modesForSuper,
              sysfs,
              effectiveChip,
              effectiveLine);
        this.delegate = delegate;
        this.overrides = overrides != null
                ? EnumSet.copyOf(overrides)
                : EnumSet.noneOf(DeviceMode.class);
    }

    PinInfo delegate() {
        return delegate;
    }

    @Override
    public boolean isSupported(DeviceMode mode) {
        return delegate.isSupported(mode) || overrides.contains(mode);
    }

    @Override
    public Collection<DeviceMode> getModes() {
        if (overrides.isEmpty()) {
            return delegate.getModes();
        }
        Collection<DeviceMode> original = delegate.getModes();
        if (original.isEmpty()) {
            return EnumSet.copyOf(overrides);
        }
        // Combined EnumSet
        EnumSet<DeviceMode> combined = EnumSet.copyOf(original);
        combined.addAll(overrides);
        return combined;
    }

    @Override
    public boolean isDigitalInputSupported()  { return isSupported(DeviceMode.DIGITAL_INPUT); }
    @Override
    public boolean isDigitalOutputSupported() { return isSupported(DeviceMode.DIGITAL_OUTPUT); }
    @Override
    public boolean isPwmOutputSupported()     { return isSupported(DeviceMode.PWM_OUTPUT); }
    @Override
    public boolean isServoSupported()         { return isSupported(DeviceMode.SERVO); }
    @Override
    public boolean isAnalogInputSupported()   { return isSupported(DeviceMode.ANALOG_INPUT); }
    @Override
    public boolean isAnalogOutputSupported()  { return isSupported(DeviceMode.ANALOG_OUTPUT); }
}
