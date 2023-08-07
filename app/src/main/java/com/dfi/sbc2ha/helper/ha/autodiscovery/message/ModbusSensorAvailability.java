package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SensorDeviceClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.Register;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.RegisterBase;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class ModbusSensorAvailability extends IterableAvailability {

    @JsonIgnore
    private final String deviceId;
    @JsonIgnore
    private final ModbusSensorDefinition def;
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement;
    @JsonProperty("state_class")
    String stateClass;
    @JsonProperty("value_template")
    String valueTemplate;
    @JsonProperty("device_class")
    String deviceClass;
    @JsonIgnore
    private Register register;
    @JsonIgnore
    private RegisterBase baserRegister;


    public ModbusSensorAvailability(ModbusSensorConfig sensorConfig, ModbusSensorDefinition def) {
        super("", "", HaDeviceType.SENSOR, SbcDeviceType.SENSOR);
        this.deviceId = sensorConfig.getName().replace(" ", "");
        this.def = def;

        DeviceAvailability deviceAvailability = getDevice();
        deviceAvailability.setName(sensorConfig.getName().toUpperCase());
        deviceAvailability.setModel(sensorConfig.getModel().toUpperCase());
        deviceAvailability.setIdentifiers(List.of(deviceId));

        getAvailability().get(0).put(TOPIC, formatTopic(Availability.topicPrefix, deviceId + STATE));


        setUniqueId(Availability.topicPrefix + getId().toLowerCase() + sensorConfig.getName().toLowerCase());
    }


    private void nextRegister() {
        if (register == null) {
            baserRegister = def.getFirstBaserRegister();
            register = def.getFirstRegister();
            return;
        }

        if (register == def.getLastRegister(baserRegister)) {
            baserRegister = def.nextBaseRegister(baserRegister);
            register = baserRegister.getRegisters().get(0);
            return;
        }
        register = def.nextRegister(baserRegister, register);
    }

    @Override
    public String getTopicPrefix() {
        String topicPrefix = super.getTopicPrefix();
        return topicPrefix + deviceId;
    }

    @Override
    public IterableAvailability iterator() {
        baserRegister = null;
        register = null;
        return this;
    }

    @Override
    public boolean hasNext() {
        if (baserRegister == def.getLastBaserRegister()) {
            return register != def.getLastRegister();
        }
        return true;
    }

    @Override
    public ModbusSensorAvailability next() {
        nextRegister();

        setId(register.getId());
        setName(register.getName());

        setStateTopic(getStateTopic(register));
        setUniqueId(Availability.topicPrefix + getStateDeviceType().toLowerCase() + deviceId + getId());

        SensorDeviceClassType deviceClassType = register.getDeviceClass() == null ? null : register.getDeviceClass();
        deviceClass = register.getDeviceClass() == null ? null : deviceClassType.getLabel();
        stateClass = register.getStateClass().getLabel();
        unitOfMeasurement = register.getUnitOfMeasurement();
        valueTemplate = "{{ value_json | " + register.getHaFilter() + " }}";

        return this;
    }

    public String getStateTopic(Register register) {
        return formatTopic(Availability.topicPrefix, getStateDeviceTypeName(), deviceId, register.getName());
    }
}
