package com.diozero.adapter.modbus;

import com.diozero.api.RuntimeIOException;
import com.diozero.api.SerialConstants;
import com.diozero.api.SerialDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DiozeroSerialPortAdapter implements net.solarnetwork.io.modbus.serial.SerialPort {

    private final String deviceFilename;
    private SerialDevice serialDevice;

    public DiozeroSerialPortAdapter(SerialDevice serialDevice) {
        deviceFilename = serialDevice.getDeviceFilename();
        this.serialDevice = serialDevice;
    }

    public DiozeroSerialPortAdapter(String name) {
        this.deviceFilename = name;
    }

    private static SerialConstants.Parity getParity(net.solarnetwork.io.modbus.serial.SerialParity parity) {
        switch (parity) {
            default:
            case None:
                return SerialConstants.Parity.NO_PARITY;

            case Odd:
                return SerialConstants.Parity.ODD_PARITY;

            case Even:
                return SerialConstants.Parity.EVEN_PARITY;

            case Mark:
                return SerialConstants.Parity.MARK_PARITY;

            case Space:
                return SerialConstants.Parity.SPACE_PARITY;
        }
    }

    @Override
    public String getName() {
        return serialDevice.getDeviceFilename();
    }

    @Override
    public void open(net.solarnetwork.io.modbus.serial.SerialParameters parameters) {
        if (serialDevice == null) {

            SerialConstants.DataBits dataBits = SerialConstants.DataBits.valueOf("CS" + parameters.getDataBits());
            SerialConstants.StopBits stopBits = parameters.getStopBits().getCode() == 1 ?
                    SerialConstants.StopBits.ONE_STOP_BIT : SerialConstants.StopBits.TWO_STOP_BITS;

            try {
                Constructor<SerialDevice> declaredConstructor = SerialDevice.class.getDeclaredConstructor(String.class, int.class, SerialConstants.DataBits.class, SerialConstants.StopBits.class
                        , SerialConstants.Parity.class,
                        boolean.class, int.class, int.class);
                declaredConstructor.setAccessible(true);

                 serialDevice = declaredConstructor.newInstance(deviceFilename, parameters.getBaudRate(),
                        dataBits,
                        stopBits,
                        getParity(parameters.getParity()),
                        false, 1, 1000);

                 return;

            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }


/*            serialDevice = SerialDevice.builder(deviceFilename)
                    .setBaud(parameters.getBaudRate())
                    .setDataBits(SerialConstants.DataBits.valueOf("CS" + parameters.getDataBits()))
                    .setStopBits(parameters.getStopBits().getCode() == 1 ?
                            SerialConstants.StopBits.ONE_STOP_BIT : SerialConstants.StopBits.TWO_STOP_BITS)
                    .setParity(getParity(parameters.getParity()))
                    .build();*/
        }
    }

    @Override
    public void close() throws IOException {
        serialDevice.close();
        serialDevice = null;
    }

    @Override
    public boolean isOpen() {
        return serialDevice != null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            if (!this.isOpen()) {
                throw new IOException("Serial port [" + this.deviceFilename + "] is not open.");
            } else {
                return new SerialPortInputStream();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error opening serial port [" + this.deviceFilename + "] input stream: " + e, e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            if (!this.isOpen()) {
                throw new IOException("Serial port [" + this.deviceFilename + "] is not open.");
            } else {
                return new SerialPortOutputStream();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error opening serial port [" + this.deviceFilename + "] output stream: " + e, e);
        }
    }

    // InputStream interface class
    private final class SerialPortInputStream extends InputStream {
        private final byte[] byteBuffer = new byte[1];

        @Override
        public int available() throws RuntimeIOException {
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }
            return serialDevice.bytesAvailable();
        }

        @Override
        public int read() throws RuntimeIOException {
            // Perform error checking
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }
            // Read from the serial port
            int numRead = serialDevice.read(byteBuffer);
            if (numRead == 0) {
                throw new RuntimeIOException("The read operation timed out before any data was returned.");
            }
            return (numRead < 0) ? -1 : ((int) byteBuffer[0] & 0xFF);
        }

        @Override
        public int read(byte[] b) throws NullPointerException, RuntimeIOException {
            // Perform error checking
            if (b == null) {
                throw new NullPointerException("A null pointer was passed in for the read buffer.");
            }
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }
            if (b.length == 0) {
                return 0;
            }

            // Read from the serial port

            int numRead = serialDevice.read(b);
            if ((numRead == 0))
                throw new RuntimeIOException("The read operation timed out before any data was returned.");
            return numRead;
        }

        @Override
        public int read(byte[] b, int off, int len) throws NullPointerException, IndexOutOfBoundsException, RuntimeIOException {
            // Perform error checking
            if (b == null) {
                throw new NullPointerException("A null pointer was passed in for the read buffer.");
            }
            if ((len < 0) || (off < 0) || (len > (b.length - off))) {
                throw new IndexOutOfBoundsException("The specified read offset plus length extends past the end of the specified buffer.");
            }
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }
            if ((b.length == 0) || (len == 0)) {
                return 0;
            }

            // Read from the serial port
            int numRead = 0;
            byte[] buf = new byte[len];
            numRead = serialDevice.read(buf);
            System.arraycopy(buf, 0, b, off, buf.length);
            if (numRead == 0) {
                throw new RuntimeIOException("The read operation timed out before any data was returned.");
            }
            return numRead;
        }

        @Override
        public long skip(long n) throws RuntimeIOException {
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }
            byte[] buffer = new byte[(int) n];
            serialDevice.read(buffer);
            return buffer.length;
        }
    }

    // OutputStream interface class
    private final class SerialPortOutputStream extends OutputStream {
        private final byte[] byteBuffer = new byte[1];

        public SerialPortOutputStream() {
        }

        @Override
        public final void write(int b) throws RuntimeIOException {
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }
            byteBuffer[0] = (byte) (b & 0xFF);
            serialDevice.write(byteBuffer);
        }

        @Override
        public void write(byte[] b) throws NullPointerException, RuntimeIOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws NullPointerException, IndexOutOfBoundsException, RuntimeIOException {
            // Perform error checking
            if (b == null) {
                throw new NullPointerException("A null pointer was passed in for the write buffer.");
            }
            if ((len < 0) || (off < 0) || ((off + len) > b.length)) {
                throw new IndexOutOfBoundsException("The specified write offset plus length extends past the end of the specified buffer.");
            }

            // Always ensure that the port has not been closed
            if (serialDevice == null) {
                throw new RuntimeIOException("This port appears to have been shutdown or disconnected.");
            }

            byte[] buff = new byte[len];
            System.arraycopy(b, off, buff, 0, len);
            serialDevice.write(buff);
        }
    }
}
