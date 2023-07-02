package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ThermBusSensorConfig;
import com.dfi.sbc2ha.helper.deserializer.BigIntegerSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class OneWireTherm extends ThermBusSensorConfig {

    @JsonSerialize(using = BigIntegerSerializer.class)
    BigInteger address;

    public OneWireTherm() {
        super();
    }
}
