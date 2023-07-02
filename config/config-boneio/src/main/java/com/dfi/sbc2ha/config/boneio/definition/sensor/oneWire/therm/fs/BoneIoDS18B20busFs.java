package com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.fs;


import com.dfi.sbc2ha.config.boneio.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.BoneIoDS18B20;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoDS18B20busFs extends BoneIoDS18B20 {


    public BoneIoDS18B20busFs() {
        super();
        platform = PlatformType.DALLAS;
    }
}
