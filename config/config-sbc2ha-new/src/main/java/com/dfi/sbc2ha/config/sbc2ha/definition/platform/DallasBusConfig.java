package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DallasBusConfig extends PlatformConfig{
    public DallasBusConfig() {
        platform = PlatformType.DALLAS;
    }
}
