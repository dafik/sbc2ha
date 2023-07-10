package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "platform",
        include = JsonTypeInfo.As.EXISTING_PROPERTY
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DallasBusConfig.class, name = "dallas"),
        @JsonSubTypes.Type(value = Mcp23017BusConfig.class, name = "mcp23017"),
        @JsonSubTypes.Type(value = Ds2482BusConfig.class, name = "ds2482"),
        @JsonSubTypes.Type(value = I2cBusConfig.class, name = "i2c"),
        @JsonSubTypes.Type(value = Lm75BusConfig.class, name = "lm75"),
        @JsonSubTypes.Type(value = ModbusBusConfig.class, name = "modbus"),
        @JsonSubTypes.Type(value = MqttConfig.class, name = "mqtt"),
        @JsonSubTypes.Type(value = OledConfig.class, name = "oled"),
})
@Data
public abstract class PlatformConfig {
    String id;
    PlatformType platform;
}
