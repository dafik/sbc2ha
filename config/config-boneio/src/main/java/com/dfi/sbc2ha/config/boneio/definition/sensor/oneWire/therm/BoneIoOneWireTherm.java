package com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm;


import com.dfi.sbc2ha.config.boneio.definition.sensor.BoneIoThermSensorConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BoneIoOneWireTherm extends BoneIoThermSensorConfig {

    BigInteger address;

    public BoneIoOneWireTherm() {
        super();
    }
}
