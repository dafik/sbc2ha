package com.dfi.sbc2ha.helper.bus;

import com.dfi.sbc2ha.bus.Modbus;
import com.dfi.sbc2ha.config.sbc2ha.definition.bus.BusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.bus.FsBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.bus.ModbusBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.UartType;
import com.dfi.sbc2ha.exception.MissingHardwareException;
import com.dfi.sbc2ha.helper.BoneIoBBB;
import com.dfi.sbc2ha.helper.ConfigurePin;
import com.dfi.sbc2ha.sensor.SensorFactory;
import com.diozero.api.I2CDevice;
import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;
import com.diozero.devices.mcp23xxx.MCP23xxx;
import com.diozero.devices.oneWire.bus.DS248.DS2482Bus;
import com.diozero.devices.oneWire.bus.fs.FsBus;
import com.diozero.util.Diozero;

import java.util.Map;

public class BusFactory {

    public static MCP23017 createMcp(BusConfig config) {
        //int address = Integer.parseInt(config.getAddress());
        int address = config.getAddress();

        return new MCP23017(BoneIoBBB.I2C_CONTROLLER, address, MCP23xxx.INTERRUPT_GPIO_NOT_SET);
    }

    public static DS2482Bus createOneWireDS2482(BusConfig config) {

        //int address = Integer.parseInt(config.getAddress());
        int address = config.getAddress();

        I2CDevice i2CDevice = I2CDevice.builder(address).setController(BoneIoBBB.I2C_CONTROLLER).build();

        return DS2482Factory.setupDS2482Bus(i2CDevice);
    }

    public static Modbus createModbus(ModbusBusConfig busConfig) throws MissingHardwareException {
        UartType uart = busConfig.getUart();

        PinInfo txPinInfo = SensorFactory.searchPinInfo(uart.uartConfig.tx);
        PinInfo rxPinInfo = SensorFactory.searchPinInfo(uart.uartConfig.rx);
        ConfigurePin.configureUart(txPinInfo);
        ConfigurePin.configureUart(rxPinInfo);

        Modbus modbus = new Modbus(busConfig.getUart());
        Diozero.registerForShutdown(modbus);

        return modbus;
    }

    public static FsBus createOneWireFs(FsBusConfig busConfig) {
        return new FsBus();
    }

    public static <T> T getBus(String busId, Map<String, Object> busMap, Class<T> busClass) {
        Object bus;
        try {
            bus = busMap.get(busId);
            if (busClass == null) {
                throw new IllegalArgumentException("bus " + busId + " not found");
            }

        } catch (ClassCastException e) {
            //– if the key is of an inappropriate type for this map (optional)
            throw new IllegalArgumentException("wrong busId: " + busId);
        } catch (NullPointerException e) {
            //– if the specified key is null and this map does not permit null keys
            throw new IllegalArgumentException("bus key is null");
        }

        if (busClass.isAssignableFrom(busClass)) {
            return busClass.cast(bus);
        }
        throw new IllegalArgumentException("bus is wrong type excepted: " + busClass.getCanonicalName() + " was: " + bus.getClass().getCanonicalName());
    }
}
