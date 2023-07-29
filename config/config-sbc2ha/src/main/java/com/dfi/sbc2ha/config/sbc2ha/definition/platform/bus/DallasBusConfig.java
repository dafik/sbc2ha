package com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DallasBusConfig extends BusConfig {

    public DallasBusConfig() {

        setPlatform(PlatformType.DALLAS);
    }
}
