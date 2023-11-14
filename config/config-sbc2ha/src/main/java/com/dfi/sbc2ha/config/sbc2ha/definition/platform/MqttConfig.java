package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MqttConfig extends PlatformConfig {
    public static final String BONE_IO = "boneIO";

    String host;

    String username;

    String password;

    int port = 1883;

    @JsonProperty("topic_prefix")
    String topicPrefix = BONE_IO;

    @JsonProperty("ha_discovery")
    HaDiscoveryConfig haDiscovery = new HaDiscoveryConfig();

    public MqttConfig() {
        platform = PlatformType.MQTT;
    }

    @Data
    public static class HaDiscoveryConfig {
        public static final String HOMEASSISTANT = "homeassistant";

        boolean enabled = true;

        @JsonProperty("topic_prefix")
        String topicPrefix = HOMEASSISTANT;
    }
}
