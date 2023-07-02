package com.diozero.adapter.onewire;


import com.diozero.api.I2CDevice;
import com.diozero.util.Hex;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DiozeroI2CAdapter {


    private final I2CDevice device;

    public DiozeroI2CAdapter(I2CDevice device) {
        this.device = device;
    }


    public String getName() {
        com.dalsemi.onewire.utils.Address.toString(24);
        return "i2c " + device.getController() + ":" + Hex.encode((byte) device.getAddress());

    }

    public boolean isOpen() {
        return true;
    }


    public void close() throws IOException {
        device.close();
    }

    public void write(ByteBuffer wrap) throws IOException {
        device.writeBytes(wrap);
    }


    public void write(int register, int registerSize, ByteBuffer command) throws IOException {

    }

    public int read(ByteBuffer byteToRead) throws IOException {
        byte[] bytes = device.readBytes(byteToRead.limit());
        byteToRead.put(bytes);

        return 1;
    }


    public int read(byte register, int i, ByteBuffer byteToRead) throws IOException {
        return 0;
    }
}
