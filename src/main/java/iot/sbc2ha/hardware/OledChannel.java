package iot.sbc2ha.hardware;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SH1106 OLED display channel on an I2C bus.
 *
 * <p>References a chip by {@code bus} id (declared in the profile's
 * {@code chips:} section).</p>
 *
 * <pre>
 * type: oled
 * bus: oled1
 * </pre>
 * <p>
 * The chip's I2C address/bus are resolved from the referenced chip.
 * </p>
 */
public final class OledChannel extends PhysicalChannel {

    public OledChannel() {}

    @JsonCreator
    public OledChannel(@JsonProperty("bus") String bus) {
        super(bus);
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.OLED;
    }

    @Override
    public String toString() {
        return "OledChannel{bus='" + bus() + "'}";
    }
}
