package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ResistanceDirectionType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SingleSourceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResistanceConfig extends SingleSourceConfig {

    /**
     * The type of circuit, one of DOWNSTREAM or UPSTREAM.
     * required
     */
    ResistanceDirectionType direction;

    /**
     * The value of the resistor with a constant value.
     * required
     */
    String resistor;

    /**
     * The reference voltage. Defaults to 1.8V.
     * optional
     */
    @JsonProperty("reference_voltage")
    String referenceVoltage;

    public ResistanceConfig(String sensor) {
        super(sensor);
        platform = PlatformType.RESISTANCE;
    }

    public ResistanceConfig() {
        super();
        platform = PlatformType.RESISTANCE;
    }
}
