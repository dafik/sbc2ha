package com.dfi.sbc2ha.state;


import com.dfi.sbc2ha.actuator.Cover;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.dfi.sbc2ha.state.device.DeviceState;
import com.dfi.sbc2ha.state.sensor.BinaryState;
import com.dfi.sbc2ha.state.sensor.ButtonState;
import com.dfi.sbc2ha.state.sensor.ScalarState;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActuatorState.class, name = "actuator"),
        @JsonSubTypes.Type(value = BinaryState.class, name = "binary"),
        @JsonSubTypes.Type(value = ButtonState.class, name = "button"),
        @JsonSubTypes.Type(value = Cover.CoverState.class, name = "cover"),
        @JsonSubTypes.Type(value = DeviceState.class, name = "device"),
        @JsonSubTypes.Type(value = ScalarState.class, name = "scalar"),
})

public interface State {
    String name();
}
