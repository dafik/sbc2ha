package com.dfi.sbc2ha.config.sbc2ha.definition.modbus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RegisterBase {

    int base;
    int length;
    List<Register> registers = new ArrayList<>();
    @JsonProperty("register_type")
    ModbusRegisterType modbusRegisterType = ModbusRegisterType.INPUT;

}
