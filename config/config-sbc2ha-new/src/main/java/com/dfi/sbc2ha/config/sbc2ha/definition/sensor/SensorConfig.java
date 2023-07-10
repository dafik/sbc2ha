package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.ModbusBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.FakeInputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.ds2482.DS18B20busDS2482;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.fs.DS18B20busFs;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "platform",
        include = JsonTypeInfo.As.EXISTING_PROPERTY
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdcSensorConfig.class, name = "analog"),
        @JsonSubTypes.Type(value = DS18B20busFs.class, name = "dallas"),
        @JsonSubTypes.Type(value = DS18B20busDS2482.class, name = "ds2482"),
        @JsonSubTypes.Type(value = FakeInputConfig.class, name = "gpio"),
        @JsonSubTypes.Type(value = Lm75SensorConfig.class, name = "lm75"),
        @JsonSubTypes.Type(value = ModbusBusConfig.class, name = "modbus"),
})
@Data
public abstract class SensorConfig {
    protected PlatformType platform;

    protected String id;

    @JsonProperty("show_in_ha")
    protected boolean showInHa = true;

    public SensorConfig() {
    }
}

