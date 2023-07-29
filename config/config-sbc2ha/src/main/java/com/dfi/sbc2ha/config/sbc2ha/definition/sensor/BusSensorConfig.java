package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "platform",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = DS18B20busDS2482.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DS18B20busFs.class, name = "dallas"),
        @JsonSubTypes.Type(value = DS18B20busDS2482.class, name = "ds2482"),
})
@Getter
@Setter
public abstract class BusSensorConfig extends ScheduledSensorConfig {

    @JsonProperty("bus_id")
    protected String busId;

    public BusSensorConfig() {
        super();
    }

}
