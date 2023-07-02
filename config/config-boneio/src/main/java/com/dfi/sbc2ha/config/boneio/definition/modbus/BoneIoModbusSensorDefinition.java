package com.dfi.sbc2ha.config.boneio.definition.modbus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BoneIoModbusSensorDefinition {
    String model;
    @JsonProperty("registers_base")
    List<BoneIoRegisterBase> registersBase = new ArrayList<>();

    @JsonIgnore
    public BoneIoRegister getFirstRegister() {
        return registersBase.get(0).getRegisters().get(0);
    }
}
