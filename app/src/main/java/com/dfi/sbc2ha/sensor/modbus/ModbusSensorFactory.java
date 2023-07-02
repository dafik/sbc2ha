package com.dfi.sbc2ha.sensor.modbus;

import com.dfi.sbc2ha.bus.Modbus;
import com.dfi.sbc2ha.config.sbc2ha.definition.bus.ModbusBusConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.helper.bus.BusFactory;
import com.dfi.sbc2ha.helper.bus.ModbusFactory;

import java.util.Map;

public class ModbusSensorFactory {

    public static ModbusSensor createDevice(ModbusSensorConfig config, Map<String, Object> busMap) {

        Modbus bus = BusFactory.getBus(ModbusBusConfig.BUS_ID, busMap, Modbus.class);
        ModbusSensorDefinition def = ModbusFactory.getDevice(config.getModel());

        return new ModbusSensor(
                config.getId(),
                config.getId().replace(" ", ""),
                config.getAddress(),
                config.getUpdateInterval(),
                bus,
                def);

    }
}
