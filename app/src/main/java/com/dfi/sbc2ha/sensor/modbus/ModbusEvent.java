package com.dfi.sbc2ha.sensor.modbus;

import com.dfi.sbc2ha.config.sbc2ha.definition.modbus.Register;
import com.dfi.sbc2ha.sensor.StateEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusEvent extends StateEvent<ModbusState> {

    @JsonIgnore
    private Register register;
    private Number value;

    public ModbusEvent() {
        super();
    }

    public ModbusEvent(long epochTime, long nanoTime) {
        super(epochTime, nanoTime);

    }

    public ModbusEvent(long epochTime, long nanoTime, ModbusState state) {
        super(epochTime, nanoTime, state);

    }

    public ModbusEvent(long epochTime, long nanoTime, ModbusState state, Register register, Number value) {
        super(epochTime, nanoTime, state);
        this.register = register;
        this.value = value;
    }

    public ModbusEvent(ModbusState state, Register register, Number value) {
        super(state);
        this.register = register;
        this.value = value;
    }
}
