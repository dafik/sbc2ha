package com.dfi.sbc2ha.config.boneio.definition.modbus;

public enum BoneIoModbusRegisterType {
    /**
     * Coil (toggle) type.
     */
    COIL(0),
    /**
     * Discrete (input) type.
     */
    DISCRETE(1),
    /**
     * Holding (output) type.
     */
    HOLDING(3),
    /**
     * Input type.
     */
    INPUT(4),
    /**
     * Diagnostic information.
     */
    DIAGNOSTIC(-1);

    private final int type;

    BoneIoModbusRegisterType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
