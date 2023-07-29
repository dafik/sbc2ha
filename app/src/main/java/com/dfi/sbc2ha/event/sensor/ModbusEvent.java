package com.dfi.sbc2ha.event.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.Register;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("modbus")
public class ModbusEvent extends ScalarEvent {

    @JsonIgnore
    private final Register register;

    public ModbusEvent(Register register, float value) {
        super(value);
        this.register = register;
    }
}
