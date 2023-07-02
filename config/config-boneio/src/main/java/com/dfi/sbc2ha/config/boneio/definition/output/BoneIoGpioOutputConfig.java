package com.dfi.sbc2ha.config.boneio.definition.output;

import com.dfi.sbc2ha.config.boneio.definition.enums.OutputKindType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoGpioOutputConfig extends BoneIoOutputConfig {


    public BoneIoGpioOutputConfig() {
        super();
        kind = OutputKindType.GPIO;
    }
}
