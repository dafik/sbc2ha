package com.dfi.sbc2ha.config.boneio.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BoneIoBMqttConfig {

    /*
        host: 192.168.1.10
        username: boneio
        password: boneio
        topic_prefix: boneio-2
        ha_discovery:
            enabled: yes
    */
    //@NonNull
    String host;
    String username;
    String password;
    int port = 1883;
    @JsonProperty("topic_prefix")
    String topicPrefix = "boneio";

    @JsonProperty("ha_discovery")
    BoneIoHaDiscoveryConfig haDiscovery = new BoneIoHaDiscoveryConfig();
}
