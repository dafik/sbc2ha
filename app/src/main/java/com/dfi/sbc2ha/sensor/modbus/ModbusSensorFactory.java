package com.dfi.sbc2ha.sensor.modbus;

import com.dfi.sbc2ha.bus.ModbusBus;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.helper.bus.ModbusFactory;
import com.dfi.sbc2ha.modbus.Modbus;

public class ModbusSensorFactory {

    public static ModbusSensor createDevice(ModbusSensorConfig config, ModbusBus bus) {

        Modbus busInternal = bus.getBus();
        ModbusSensorDefinition def = ModbusFactory.getDevice(config.getModel());

        return new ModbusSensor(
                config.getId(),
                config.getId().replace(" ", ""),
                config.getAddress(),
                config.getUpdateInterval(),
                busInternal,
                def);

    }
}
