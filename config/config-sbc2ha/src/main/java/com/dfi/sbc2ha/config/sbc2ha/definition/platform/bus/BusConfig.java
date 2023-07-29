package com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.PlatformConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public abstract class BusConfig extends PlatformConfig {
    @JsonProperty("bus_id")
    String busId;

}
