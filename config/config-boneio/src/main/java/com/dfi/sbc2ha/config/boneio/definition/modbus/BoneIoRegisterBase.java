package com.dfi.sbc2ha.config.boneio.definition.modbus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BoneIoRegisterBase {

    int base;
    int length;
    List<BoneIoRegister> registers = new ArrayList<>();
    @JsonProperty("register_type")
    BoneIoModbusRegisterType modbusRegisterType = BoneIoModbusRegisterType.INPUT;

}
