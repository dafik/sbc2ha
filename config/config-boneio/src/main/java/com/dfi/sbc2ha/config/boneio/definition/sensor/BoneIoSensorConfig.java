package com.dfi.sbc2ha.config.boneio.definition.sensor;


import com.dfi.sbc2ha.config.boneio.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.boneio.definition.filter.BoneIoValueFilterType;
import com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.ds2482.BoneIoDS18B20busDS2482;
import com.dfi.sbc2ha.config.boneio.definition.sensor.oneWire.therm.fs.BoneIoDS18B20busFs;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.Duration;
import java.util.*;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "platform",
        defaultImpl = BoneIoDS18B20busDS2482.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoneIoDS18B20busFs.class, name = "dallas"),
        @JsonSubTypes.Type(value = BoneIoDS18B20busDS2482.class, name = "ds2482"),
})
public abstract class BoneIoSensorConfig {

    /*
         - id: t4
           address: 0x693cf2e381cf9528
           bus_id: ds2482
           update_interval: 15s


         - id: temperature
           address: 0x6e0300a279d76428
           bus_id: mydallas
           update_interval: 60s

           platform: dallas
           filters:
             - round: 2
             - offset: 5
    */
    protected PlatformType platform;

    String id;
    @JsonProperty("bus_id")
    String busId;

    @JsonProperty("update_interval")
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    Duration updateInterval = DurationStyle.detectAndParse("60s");

    public List<Map<BoneIoValueFilterType, Number>> filters = new ArrayList<>();

    @JsonProperty("show_in_ha")
    boolean showInHa = true;

}
