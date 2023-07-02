package com.dfi.sbc2ha.config.sbc2ha.definition.modbus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModbusSensorDefinition {
    String model;
    @JsonProperty("registers_base")
    List<RegisterBase> registersBase = new ArrayList<>();

    @JsonIgnore
    public Register getFirstRegister() {
        return registersBase.get(0).getRegisters().get(0);
    }
}
