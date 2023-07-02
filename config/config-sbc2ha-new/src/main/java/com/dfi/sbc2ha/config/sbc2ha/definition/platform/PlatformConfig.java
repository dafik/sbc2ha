package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import lombok.Data;

@Data
public abstract class PlatformConfig {
    String id;
    PlatformType platform;
}
