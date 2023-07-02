package com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.ds2482;

import com.dfi.sbc2ha.config.boneio.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.BoneIoDS18B20;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoDS18B20busDS2482 extends BoneIoDS18B20 {


    public BoneIoDS18B20busDS2482() {
        super();
        platform = PlatformType.DS2482;
    }
}
