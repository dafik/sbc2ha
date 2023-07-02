package com.dfi.sbc2ha.config.boneio.definition.modbus;


import com.dfi.sbc2ha.config.boneio.definition.enums.ModbusReturnType;
import com.dfi.sbc2ha.config.boneio.definition.enums.StateClassType;
import com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha.BoneIoSensorDeviceClassType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class BoneIoRegister {
    String name;
    int address;
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement;
    @JsonProperty("state_class")
    StateClassType stateClass;
    @JsonProperty("device_class")
    BoneIoSensorDeviceClassType deviceClass;
    @JsonProperty("return_type")
    ModbusReturnType returnType;

    @JsonProperty("register_type")
    BoneIoModbusRegisterType modbusRegisterType = BoneIoModbusRegisterType.INPUT;

    @JsonProperty("ha_filter")
    String haFilter = "round(2)";

    @JsonIgnore
    public String getId() {
        return name.replace("_", "");
    }

    @JsonIgnore
    public Number decode(short[] responseData, int base) {
        switch (returnType) {
            case FLOAT32:
                return float32(responseData, base);
            case FLOATSOFAR:
                return floatsofar(responseData, base);
            case MULTIPLY0_1:
                return multiply0_1(responseData, base);
            case MULTIPLY0_01:
                return multiply0_01(responseData, base);
            case MULTIPLY10:
                return multiply10(responseData, base);
            default:
            case REGULAR_RESULT:
                return regular_result(responseData, base);
        }
    }

    private float float32(short[] result, int base) {
        int low = result[address - base];
        int high = result[address - base + 1];
        byte[] data = new byte[4];
        data[0] = (byte) (high & 0xFF);
        data[1] = (byte) (high >> 8);
        data[2] = (byte) (low & 0xFF);
        data[3] = (byte) (low >> 8);
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private int floatsofar(short[] result, int base) {
        int low = result[address - base];
        int high = result[address - base + 1];
        return high + low;
    }

    private float multiply0_1(short[] result, int base) {
        int low = result[address - base];
        return Math.round(low * 0.1f * 10000.0f) / 10000.0f;
    }

    private float multiply0_01(short[] result, int base) {
        int low = result[address - base];
        return Math.round(low * 0.01f * 10000.0f) / 10000.0f;
    }

    private float multiply10(short[] result, int base) {
        int low = result[address - base];
        return Math.round(low * 10.0f * 10000.0f) / 10000.0f;
    }

    private int regular_result(short[] result, int base) {
        return result[address - base];
    }

}
