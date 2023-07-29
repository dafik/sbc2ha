package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ModbusSensorDefinition {
    String model;
    @JsonProperty("registers_base")
    List<RegisterBase> registersBase = new ArrayList<>();

    @JsonIgnore
    public Register getFirstRegister() {
        return getFirstBaserRegister().getRegisters().get(0);
    }

    @JsonIgnore
    public Register getLastRegister(RegisterBase base) {
        List<Register> registers = base.getRegisters();
        return registers.get(registers.size() - 1);
    }

    public Register getLastRegister() {
        return getLastRegister(getLastBaserRegister());
    }

    @JsonIgnore
    public RegisterBase getFirstBaserRegister() {
        return registersBase.get(0);
    }

    @JsonIgnore
    public RegisterBase getLastBaserRegister() {
        return registersBase.get(registersBase.size() - 1);
    }

    public RegisterBase nextBaseRegister(RegisterBase baserRegister) {
        int index = registersBase.indexOf(baserRegister);
        return registersBase.get(index + 1);
    }

    public Register nextRegister(RegisterBase baserRegister, Register register) {
        List<Register> registers = baserRegister.getRegisters();
        int index = registers.indexOf(register);
        return registers.get(index + 1);
    }
}
