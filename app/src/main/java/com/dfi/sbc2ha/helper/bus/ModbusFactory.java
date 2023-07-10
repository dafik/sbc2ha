package com.dfi.sbc2ha.helper.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ModbusFactory {
    private static final Map<String, ModbusSensorDefinition> devices = new HashMap<>();
    private static final ObjectMapper om = JsonMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build();


    public static ModbusSensorDefinition getDevice(String model) {
        if (devices.containsKey(model)) {
            return devices.get(model);
        }

        String file = "modbus/" + model + ".json";
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(file);
        ModbusSensorDefinition deviceDefinition;
        try {
            deviceDefinition = om.readValue(inputStream, ModbusSensorDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        devices.put(model, deviceDefinition);
        return deviceDefinition;
    }
}
