package com.dfi.sbc2ha.config.boneio.definition.cover;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;

@Data
public class BoneioCoverConfig {
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

    /**
     * Relay ID which closes a cover. It has to be set in output section.
     * Required
     */
    @JsonProperty("close_relay")
    String closeRelay;

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

    /**
     * Send autodiscovery message to Home Assistant.
     * Optional
     */
    @JsonProperty("show_in_ha")
    boolean showInHa = true;

    /**
     * You can enable restore_state option. It’s bit experimental.
     * It saves state of relay or cover in state.json file, which is located in same directory as your config.json.
     * If output_type is None, then this value is overwritten to False!
     * Optional
     */
    @JsonProperty("restore_state")
    boolean restoreState = false;

}
