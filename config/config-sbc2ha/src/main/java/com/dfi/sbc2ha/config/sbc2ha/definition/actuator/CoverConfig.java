package com.dfi.sbc2ha.config.sbc2ha.definition.actuator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;

@Data
@JsonIgnoreProperties({"output", "momentaryTurnOn", "momentaryTurnOff"})
public class CoverConfig extends ActuatorConfig {
    /**
     * uniquely identifies this device in MQTT and Home Assistant.
     * required
     */
    String id;

    /**
     * Relay ID which opens a cover. It has to be set in output section.
     * required
     */
    @JsonProperty("open_relay")
    String openRelay;
    @JsonProperty("open_relay_bus_id")
    String openRelayBusId;
    @JsonProperty("open_relay_bus_type")
    String openRelayBusType;

    /**
     * Relay ID which closes a cover. It has to be set in output section.
     * Required
     */
    @JsonProperty("close_relay")
    String closeRelay;
    @JsonProperty("close_relay_bus_id")
    String closeRelayBusId;
    @JsonProperty("close_relay_bus_type")
    String closeRelayBusType;

    /**
     * Time to open a cover. Example 20 seconds.
     * Required
     */
    @JsonProperty("open_time")
    Duration openTime;

    /**
     * Time to close a cover. Example 30 sec.
     * Required
     */
    @JsonProperty("close_time")
    Duration closeTime;

    /**
     * Type of device to see in Home Assistant.
     * allowed_values: ['awning', 'blind', 'curtain', 'damper', 'door', 'garage', 'gate', 'shade', 'shutter', 'window']) -
     * Optional
     */
    @JsonProperty("device_class")
    String deviceClass;


}
