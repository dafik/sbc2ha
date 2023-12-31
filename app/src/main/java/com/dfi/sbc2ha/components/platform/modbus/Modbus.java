package com.dfi.sbc2ha.components.platform.modbus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusRegisterType;
import com.dfi.sbc2ha.util.Mock;
import com.diozero.adapter.modbus.DiozeroSerialPortProvider;
import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;
import lombok.extern.slf4j.Slf4j;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusTimeoutException;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.netty.RtuNettyModbusClient;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;

import java.util.concurrent.ExecutionException;

@Slf4j
public class Modbus implements DeviceInterface {

    private final ModbusClient client;
    private final boolean useDiozero;

    public Modbus(UartType uart, boolean useDiozero) {
        this.useDiozero = useDiozero;
        BasicSerialParameters params = new BasicSerialParameters();
        params.setBaudRate(9600);
        params.setReadTimeout(3000);
        params.setWaitTime(500);

        //if (useDiozero) {
            client = getModbusClientDiozero(uart, params);
        //} else {
            //client = getModbusClientJsc(uart, params);
        //}
    }

    private ModbusClient getModbusClientDiozero(UartType uart, BasicSerialParameters params) {
        params.setReadTimeout(0);
        NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig(uart.uartConfig.file, params);

        return new RtuNettyModbusClient(config, new DiozeroSerialPortProvider());
    }

/*    private ModbusClient getModbusClientJsc(UartType uart, BasicSerialParameters params) {
        NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig(uart.uartConfig.file, params);
        config.setAutoReconnect(false);

        return new RtuNettyModbusClient(config, new net.solarnetwork.io.modbus.rtu.jsc.JscSerialPortProvider());
    }*/

    public boolean isDeviceAvailable(int unitId, int address, ModbusRegisterType type) {
        if (Mock.isMockRun()) {
            return true;
        }
        try {
            client.start().get();

            RegistersModbusMessage request = RegistersModbusMessage.readRegistersRequest(ModbusBlockType.valueOf(type.getType()), unitId, address, 1);
            ModbusMessage response = client.send(request);
            RegistersModbusMessage responseMessage = response.unwrap(RegistersModbusMessage.class);

            if (useDiozero) {
                client.stop();
            }


        } catch (ExecutionException | InterruptedException e) {
            log.error("device available check error", e);
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
            if (useDiozero) {
                client.stop();
            }
            return shorts;
        } catch (ExecutionException | InterruptedException | ModbusTimeoutException e) {
            client.stop();
            log.error("read many id:{} length:{}", unitId, length, e);
            throw new RuntimeException(e);
        }
    }
}
