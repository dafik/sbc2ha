package com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.helper.deserializer.HexIntegerDeserializer;
import com.dfi.sbc2ha.helper.deserializer.HexIntegerSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public  class I2cBusConfig extends BusConfig {
    @JsonSerialize(using = HexIntegerSerializer.class)
    @JsonDeserialize(using = HexIntegerDeserializer.class)
    int address;

    public I2cBusConfig() {
        super();
        setPlatform(PlatformType.I2C);
    }
}
