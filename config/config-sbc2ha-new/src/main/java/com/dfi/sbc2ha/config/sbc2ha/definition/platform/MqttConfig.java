package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.helper.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MqttConfig extends PlatformConfig {
    String host;
    String username;
    String password;
    int port = 1883;
    @JsonProperty("topic_prefix")
    String topicPrefix = Constants.BONEIO;

    @JsonProperty("ha_discovery")
    HaDiscoveryConfig haDiscovery = new HaDiscoveryConfig();

    public MqttConfig() {
        id = "mqtt";
        platform = PlatformType.MQTT;
    }

    @Data
    public static class HaDiscoveryConfig {
        boolean enabled = true;
        @JsonProperty("topic_prefix")
        String topicPrefix = Constants.HOMEASSISTANT;
    }
}
