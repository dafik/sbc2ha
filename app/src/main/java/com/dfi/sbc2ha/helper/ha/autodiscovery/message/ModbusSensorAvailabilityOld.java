package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.deviceClass.ha.SensorDeviceClassType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.Register;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.sensor.modbus.ModbusSensor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class ModbusSensorAvailabilityOld extends Availability {

    private final String deviceId;
    @JsonProperty("unit_of_measurement")
    String unitOfMeasurement;
    @JsonProperty("state_class")
    String stateClass;
    @JsonProperty("value_template")
    String valueTemplate;
    @JsonProperty("device_class")
    String deviceClass;



    /*
        """Create Modbus Sensor availability topic for HA."""
    return {
        "availability": [{"topic": f"{topic}/{id}{STATE}"}],
        "device": {
            "identifiers": [id],
            "manufacturer": "boneIO",
            "model": model,
            "name": f"boneIO {name.upper()}",
            "sw_version": __version__,
        },
        "name": sensor_id,
        "state_topic": f"{topic}/{device_type}/{id}/{state_topic_base}",
        "unique_id": f"{topic}{sensor_id.replace('_', '').lower()}{name.lower()}",
        **kwargs,
    }
    *
    * */


    public ModbusSensorAvailabilityOld(Register register, ModbusSensor device, String model, int registerBase) {
        super(register.getId(), register.getName(), HaDeviceType.SENSOR, SbcDeviceType.SENSOR);
        this.deviceId = device.getId();

        SensorDeviceClassType deviceClassType = register.getDeviceClass() == null ? SensorDeviceClassType.NONE : register.getDeviceClass();
        deviceClass = deviceClassType.getLabel();
        stateClass = register.getStateClass().getLabel();
        unitOfMeasurement = register.getUnitOfMeasurement();
        String name = register.getName();

        valueTemplate = "{{ value_json." + register.getName() + " | " + register.getHaFilter() + " }}";

        //boneio-1/sensor/LicznikEnergi/0",

        DeviceAvailability deviceAvailability = getDevice();
        deviceAvailability.setName("boneIO " + device.getName().toUpperCase());
        deviceAvailability.setModel(model.toUpperCase());
        deviceAvailability.setIdentifiers(List.of(device.getId()));

        getAvailability().get(0).put(TOPIC, formatTopic(Availability.topicPrefix, device.getId() + STATE));


        setStateTopic(formatTopic(Availability.topicPrefix, getStateDeviceTypeName(), device.getId(), name));
        setUniqueId(Availability.topicPrefix + getId().toLowerCase() + device.getName().toLowerCase());

    }

/*
    {
    "availability": [{"topic": "boneio-1/LicznikEnergistate"}],
    "device": {
        "identifiers": ["LicznikEnergi"],
        "manufacturer": "boneIO",
        "model": "SDM630",
        "name": "boneIO LICZNIK ENERGI",
        "sw_version": "0.6.1dev3"
     },
     "name": "Voltage_Phase1",
     "state_topic": "boneio-1/sensor/LicznikEnergi/0",
     "unique_id": "boneio-1voltagephase1licznik energi",
     "unit_of_measurement": "V",
     "state_class": "measurement",
     "value_template": "{{ value_json.Voltage_Phase1 | round(2) }}",
     "device_class": "voltage"
     }
  */

    @Override
    public String getTopicPrefix() {
        String topicPrefix = super.getTopicPrefix();
        return topicPrefix + deviceId;
    }

    @Override
    public String getNodeName() {
        return deviceId + getId().toLowerCase();
    }
}
