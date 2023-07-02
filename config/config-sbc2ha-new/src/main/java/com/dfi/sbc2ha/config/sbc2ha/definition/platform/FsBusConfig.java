package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public class FsBusConfig extends PlatformConfig{
    public FsBusConfig() {
        platform = PlatformType.DALLAS;
    }
}
