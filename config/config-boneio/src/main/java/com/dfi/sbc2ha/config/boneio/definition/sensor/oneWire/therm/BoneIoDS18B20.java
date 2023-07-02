package com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm;


import com.dfi.sbc2ha.config.boneio.definition.enums.PlatformType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BoneIoDS18B20 extends BoneIoOneWireTherm {

    public BoneIoDS18B20() {
        super();
    }
}
