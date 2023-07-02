package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.helper.deserializer.HexIntegerSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public  class I2cBusConfig extends PlatformConfig {
    @JsonSerialize(using = HexIntegerSerializer.class)
    int address;

    public I2cBusConfig() {
        super();
        platform = PlatformType.I2C;
    }
}
