package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ThermBusSensorConfig;
import com.dfi.sbc2ha.helper.deserializer.BigIntegerDeserializer;
import com.dfi.sbc2ha.helper.deserializer.BigIntegerSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;


@Getter
@Setter
public abstract class OneWireTherm extends ThermBusSensorConfig {

    @JsonSerialize(using = BigIntegerSerializer.class)
    @JsonDeserialize(using = BigIntegerDeserializer.class)
    BigInteger address;

    public OneWireTherm() {
        super();
    }

    public BigInteger getAddress() {
        return address;
    }
}
