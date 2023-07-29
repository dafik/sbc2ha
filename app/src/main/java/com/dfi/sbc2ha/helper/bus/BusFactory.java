package com.dfi.sbc2ha.helper.bus;

import com.dfi.sbc2ha.bus.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.BusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.DallasBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.I2cBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.bus.ModbusBusConfig;
import com.dfi.sbc2ha.exception.MissingHardwareException;
import com.dfi.sbc2ha.helper.BoneIoBBB;
import com.dfi.sbc2ha.helper.ConfigurePin;
import com.dfi.sbc2ha.modbus.Modbus;
import com.dfi.sbc2ha.sensor.SensorFactory;
import com.diozero.api.I2CDevice;
import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;
import com.diozero.devices.mcp23xxx.MCP23xxx;
import com.diozero.devices.oneWire.bus.DS248.DS2482Bus;
import com.diozero.devices.oneWire.bus.fs.FsBusImpl;
import com.diozero.util.Diozero;

public class BusFactory {
    public static Bus<?> createBus(BusConfig busConfig) {
        if (busConfig instanceof I2cBusConfig) {
            if (busConfig.getPlatform() == PlatformType.DS2482) {
                return new OWireBus(PlatformType.DS2482, createOneWireDS2482((I2cBusConfig) busConfig), busConfig.getBusId());
            } else if (busConfig.getPlatform() == PlatformType.MCP23017) {
                return new MCP23017Bus(createMcp((I2cBusConfig) busConfig), busConfig.getBusId());
            } else if (busConfig.getPlatform() == PlatformType.LM75) {
                return new Lm75Bus(createI2c((I2cBusConfig) busConfig), busConfig.getBusId());
            }
        } else if (busConfig instanceof ModbusBusConfig) {
            return new ModbusBus(createModbus((ModbusBusConfig) busConfig), busConfig.getBusId());
        } else if (busConfig instanceof DallasBusConfig) {
            return new OWireBus(PlatformType.DALLAS, createOneWireFs(), busConfig.getBusId());
        }
        throw new IllegalArgumentException("unknown bus " + busConfig.getBusId());
    }

    private static I2CDevice createI2c(I2cBusConfig config) {

        int address = config.getAddress();
        return new I2CDevice(BoneIoBBB.I2C_CONTROLLER, address);
    }

    private static MCP23017 createMcp(I2cBusConfig config) {
        return new MCP23017(BoneIoBBB.I2C_CONTROLLER, config.getAddress(), MCP23xxx.INTERRUPT_GPIO_NOT_SET);
    }

    private static DS2482Bus createOneWireDS2482(I2cBusConfig config) {

        int address = config.getAddress();

        I2CDevice i2CDevice = I2CDevice.builder(address).setController(BoneIoBBB.I2C_CONTROLLER).build();

        return DS2482Factory.setupDS2482Bus(i2CDevice);
    }

    private static Modbus createModbus(ModbusBusConfig busConfig) throws MissingHardwareException {
        UartType uart = busConfig.getUart();

        PinInfo txPinInfo = SensorFactory.searchPinInfo(uart.uartConfig.tx);
        PinInfo rxPinInfo = SensorFactory.searchPinInfo(uart.uartConfig.rx);
        ConfigurePin.configureUart(txPinInfo);
        ConfigurePin.configureUart(rxPinInfo);

        Modbus modbus = new Modbus(busConfig.getUart());
        Diozero.registerForShutdown(modbus);

        return modbus;
    }

    private static FsBusImpl createOneWireFs() {
        return new FsBusImpl();
    }

}
