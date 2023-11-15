package com.diozero.devices.oled;


import com.diozero.api.I2CDeviceInterface;

/**
 * Comms for OLED devices.
 */

public interface ShOledCommunicationChannel extends AutoCloseable {
    /**
     * Send to the device.
     * @param buffer data to send
     */
    void write(byte... buffer);

    /**
     * Send parts to the device.
     * @param buffer data to send
     * @param offset offset
     * @param length length
     */
    void write(byte[] buffer, int offset, int length);

    @Override
    void close();

    /**
     * Optionally, reset the device.
     */
    default void reset() {

    }

    /**
     * Sends a "command".
     * @param commands the set of commands to send
     */
    void sendCommand(byte[] commands);

    /**
     * Sends a "data buffer".
     * @param buffer the buffer
     */
    void sendData(byte[] buffer);

    /**
     * Send part of a "data buffer"
     * @param buffer the buffer
     * @param offset offset
     * @param length size
     */
    void sendData(byte[] buffer, int offset, int length);

    /**
     * SPI channel, with a data pin and a reset pin.
     */
 /*   class SpiCommunicationChannel implements SkOledCommunicationChannel {
        public static final int SPI_FREQUENCY = 8_000_000;
        private final SpiDevice device;
        private final DigitalOutputDevice dcPin;
        private final DigitalOutputDevice resetPin;

        public SpiCommunicationChannel(int chipSelect, int controller, int spiFrequency, DigitalOutputDevice dcPin,
                                       DigitalOutputDevice resetPin) {
            this.dcPin = dcPin;
            this.resetPin = resetPin;
            device = SpiDevice.builder(chipSelect).setController(controller).setFrequency(spiFrequency).build();
        }

        @Override
        public void write(byte... buffer) {
            device.write(buffer);
        }

        @Override
        public void write(byte[] txBuffer, int txOffset, int length) {
            device.write(txBuffer, txOffset, length);
        }

        @Override
        public void close() {
            log.trace("close()");
            device.close();
        }

        @Override
        public void reset() {
            resetPin.setOn(true);
            SleepUtil.sleepMillis(1);
            resetPin.setOn(false);
            SleepUtil.sleepMillis(10);
            resetPin.setOn(true);
        }

        @Override
        public void sendCommand(byte[] commands) {
            dcPin.setOn(false);
            device.write(commands);
        }

        @Override
        public void sendData(byte[] buffer) {
            dcPin.setOn(true);
            device.write(buffer);
        }

        @Override
        public void sendData(byte[] buffer, int offset, int length) {
            dcPin.setOn(true);
            device.write(buffer, offset, length);
        }
    }
*/
    /**
     * I2C channel. Sends the buffer data to the device in configurable chunks to adjust for the I2C speed.
     */
    class I2cCommunicationChannel implements ShOledCommunicationChannel {
        public static final byte DEFAULT_I2C_COMMAND = (byte)0x00;
        public static final byte DEFAULT_I2C_DATA = (byte)0x40;

        private final I2CDeviceInterface device;
        private final byte commandByte;
        private final byte dataByte;

        public I2cCommunicationChannel(I2CDeviceInterface device) {
            this(device, DEFAULT_I2C_COMMAND, DEFAULT_I2C_DATA);
        }

        public I2cCommunicationChannel(I2CDeviceInterface device, byte commandByte, byte dataByte) {
            this.device = device;
            this.commandByte = commandByte;
            this.dataByte = dataByte;
        }

        @Override
        public void write(byte... buffer) {
            device.writeBytes(buffer);
        }

        @Override
        public void write(byte[] buffer, int offset, int length) {
            byte[] data = new byte[length];
            System.arraycopy(buffer, offset, data, 0, length);
            device.writeBytes(data);
        }

        @Override
        public void close() {
            device.close();
        }

        @Override
        public void sendCommand(byte[] commands) {
            byte[] output = new byte[2];
            output[0] = commandByte;
            for (byte command : commands) {
                output[1] = command;
                write(output);
            }
        }

        @Override
        public void sendData(byte[] buffer) {
            sendData(buffer, 0, buffer.length);
        }

        @Override
        public void sendData(byte[] buffer, int offset, int length) {
            byte[] output = new byte[length + 1];
            output[0] = dataByte;
            System.arraycopy(buffer, offset, output, 1, length);
            write(output);
        }
    }
}
