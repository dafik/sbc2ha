package com.dfi.sbc2ha.web.websocket.command.state;

import com.dfi.sbc2ha.web.websocket.command.state.actuator.GpioCommand;
import com.dfi.sbc2ha.web.websocket.command.state.sensor.BinaryCommand;
import com.dfi.sbc2ha.web.websocket.command.state.sensor.ButtonCommand;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ButtonCommand.class),
        @JsonSubTypes.Type(value = BinaryCommand.class),
        @JsonSubTypes.Type(value = GpioCommand.class),
})
public abstract class StateCommand {

}
