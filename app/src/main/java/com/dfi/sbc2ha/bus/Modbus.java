package com.dfi.sbc2ha.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import com.dfi.sbc2ha.config.sbc2ha.definition.modbus.ModbusRegisterType;
import com.dfi.sbc2ha.helper.MockHelper;
import com.diozero.adapter.modbus.DiozeroSerialPortProvider;
import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.jsc.JscSerialPortProvider;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.netty.RtuNettyModbusClient;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import org.tinylog.Logger;

import java.util.concurrent.ExecutionException;

public class Modbus implements DeviceInterface {

    private final ModbusClient client;

    public Modbus(UartType uart) {
        BasicSerialParameters params = new BasicSerialParameters();
        params.setBaudRate(9600);

        if (System.getProperty("useDiozeroSerial") != null) {
            client = getModbusClientDiozero(uart, params);
        } else {
            client = getModbusClientJsc(uart, params);
        }
    }

    private ModbusClient getModbusClientDiozero(UartType uart, BasicSerialParameters params) {
        params.setReadTimeout(0);
        NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig(uart.uartConfig.file, params);

        final ModbusClient client = new RtuNettyModbusClient(config, new DiozeroSerialPortProvider());
        return client;
    }

    private ModbusClient getModbusClientJsc(UartType uart, BasicSerialParameters params) {
        NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig(uart.uartConfig.file, params);
        config.setAutoReconnect(false);

        // create the client, passing in the JSC SerialPortProvider
        final ModbusClient client = new RtuNettyModbusClient(config, new JscSerialPortProvider());
        return client;
    }

    public boolean isDeviceAvailable(int unitId, int address, ModbusRegisterType type) {
        if (MockHelper.isMockRun()) {
            return true;
        }
        try {
            client.start().get();

            RegistersModbusMessage request = RegistersModbusMessage.readRegistersRequest(ModbusBlockType.valueOf(type.getType()), unitId, address, 1);
            ModbusMessage response = client.send(request);
            RegistersModbusMessage responseMessage = response.unwrap(RegistersModbusMessage.class);

            client.stop();


        } catch (ExecutionException | InterruptedException e) {
            Logger.error(e);
            return false;
        }
        return true;
    }

    @Override
    public void close() throws RuntimeIOException {
        if (client != null) {
            client.stop();
        }
    }

    public short[] readMany(int unitId, int address, int length, ModbusRegisterType type) {
        try {
            client.start().get();

            RegistersModbusMessage request = RegistersModbusMessage.readRegistersRequest(ModbusBlockType.valueOf(type.getType()), unitId, address, length);
            ModbusMessage response = client.send(request);
            RegistersModbusMessage responseMessage = response.unwrap(RegistersModbusMessage.class);
            short[] shorts = responseMessage.dataDecode();
            client.stop();
            return shorts;
        } catch (ExecutionException | InterruptedException e) {
            Logger.error(e);
            return new short[]{0};
        }
    }
}
