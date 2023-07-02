package com.diozero.devices;

import com.diozero.api.I2CDevice;
import com.diozero.api.RuntimeIOException;

import java.nio.ByteBuffer;

public class LM75 implements ThermometerInterface {


    private static final byte DEFAULT_I2C_ADDRESS = 0x48;
    private static final byte LM75_TEMP_REGISTER = 0;
    private static final byte LM75_CONF_REGISTER = 1;
    private static final byte LM75_THYST_REGISTER = 2;
    private static final byte LM75_TOS_REGISTER = 3;

    private static final byte LM75_CONF_SHUTDOWN = 0;
    private static final byte LM75_CONF_OS_COMP_INT = 1;
    private static final byte LM75_CONF_OS_POL = 2;
    private static final byte LM75_CONF_OS_F_QUE = 3;

    private final I2CDevice device;

    public LM75(int controller) {
        this(controller, DEFAULT_I2C_ADDRESS);
    }

    public LM75(int controller, byte address) {
        device = I2CDevice.builder(address)
                .setController(controller)
                .build();
    }

    public LM75(I2CDevice device) {
        this.device = device;
    }


    @Override
    public void close() throws RuntimeIOException {

    }

    private int float2regdata(float temp) {
        return (int) (temp * 8) << 5;
    }

    private float regdata2float(int regdata) {
        return ((float) regdata / 32) / 8;
    }

    private int getDataRegister(byte register) {
        ByteBuffer buffer = device.readI2CBlockDataByteBuffer(register, 2);
        return buffer.getShort();
    }

    private void setDataRegister(byte register, int data) {
        byte msb = (byte) (data >> 8);
        byte lsb = (byte) (data & 0xFF);
        byte[] ba = {msb, lsb};

        device.writeI2CBlockData(register, ba);
    }

    private byte getConfRegister() {
        return device.readByteData(LM75_CONF_REGISTER);

    }

    private void setConfRegister(byte data) {
        device.writeI2CBlockData(LM75.LM75_CONF_REGISTER, data);

    }

    @Override
    public float getTemperature() throws RuntimeIOException {
        int regdata = getDataRegister(LM75_TEMP_REGISTER);
        return regdata2float(regdata);
    }


    public float getTempOutputSink() {
        int regdata = getDataRegister(LM75_TOS_REGISTER);
        return regdata2float(regdata);
    }

    public void setTempOutputSink(float temp) {
        int regdata = float2regdata(temp);
        setDataRegister(LM75_TOS_REGISTER, regdata);
    }

    public float getTempHysteresis() {
        int regdata = getDataRegister(LM75_THYST_REGISTER);
        return regdata2float(regdata);
    }

    public void setTempHysteresis(float temp) {
        int regdata = float2regdata(temp);
        setDataRegister(LM75_THYST_REGISTER, regdata);
    }

    public boolean isShutdown() {
        byte conf = getConfRegister();
        return (conf & (1 << LM75_CONF_SHUTDOWN)) != 0;
    }

    public void setShutdown(boolean shutdown) {
        byte conf = getConfRegister();
        if (shutdown) {
            conf |= (1 << LM75_CONF_SHUTDOWN);
        } else {
            conf &= ~(1 << LM75_CONF_SHUTDOWN);
        }
        setConfRegister(conf);
    }


}
