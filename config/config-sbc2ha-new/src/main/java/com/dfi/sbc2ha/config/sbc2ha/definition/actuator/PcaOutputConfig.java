package com.dfi.sbc2ha.config.sbc2ha.definition.actuator;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputKindType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PcaOutputConfig extends ActuatorConfig {

    @JsonProperty("percentage_default_brightness")
    int percentageDefaultBrightnes = 1;

    public PcaOutputConfig() {
        super();
        kind = OutputKindType.PCA;
    }
}
