package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus;

public enum ModbusRegisterType {
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

    ModbusRegisterType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
