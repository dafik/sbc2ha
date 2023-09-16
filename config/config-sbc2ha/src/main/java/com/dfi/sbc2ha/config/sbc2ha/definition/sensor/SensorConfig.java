package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.AnalogSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.NtcConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.ResistanceConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSwitchConfig;
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
        include = JsonTypeInfo.As.EXISTING_PROPERTY
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DS18B20busFs.class, name = "dallas"),
        @JsonSubTypes.Type(value = DS18B20busDS2482.class, name = "ds2482"),

        @JsonSubTypes.Type(value = InputSensorConfig.class, name = "digital"),
        @JsonSubTypes.Type(value = InputSwitchConfig.class, name = "switch"),


        @JsonSubTypes.Type(value = Lm75SensorConfig.class, name = "lm75"),

        @JsonSubTypes.Type(value = ModbusSensorConfig.class, name = "modbus"),

        @JsonSubTypes.Type(value = AnalogSensorConfig.class, name = "analog"),
        @JsonSubTypes.Type(value = ResistanceConfig.class, name = "resistance"),
        @JsonSubTypes.Type(value = NtcConfig.class, name = "ntc")
})
@Getter
@Setter
public abstract class SensorConfig {
    protected PlatformType platform;

    /**
     * The name for the sensor.
     * required
     */
    protected String name;

    @JsonProperty("show_in_ha")
    protected boolean showInHa = true;

    public SensorConfig() {
    }

}
