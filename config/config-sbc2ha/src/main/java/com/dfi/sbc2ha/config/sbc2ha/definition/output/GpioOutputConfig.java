package com.dfi.sbc2ha.config.sbc2ha.definition.output;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.OutputKindType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GpioOutputConfig extends OutputConfig {


    public GpioOutputConfig() {
        super();
        kind = OutputKindType.GPIO;
    }
}
