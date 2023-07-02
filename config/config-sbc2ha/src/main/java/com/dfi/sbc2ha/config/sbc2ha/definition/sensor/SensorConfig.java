package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer;
import lombok.Data;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "platform",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = DS18B20busDS2482.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DS18B20busFs.class, name = "dallas"),
        @JsonSubTypes.Type(value = DS18B20busDS2482.class, name = "ds2482"),
})
public abstract class SensorConfig {

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
    Map<String, String> filters = new LinkedHashMap<>();

    @JsonProperty("show_in_ha")
    boolean showInHa = true;

}
