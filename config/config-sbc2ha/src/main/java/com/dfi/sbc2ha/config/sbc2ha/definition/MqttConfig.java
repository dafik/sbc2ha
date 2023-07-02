package com.dfi.sbc2ha.config.sbc2ha.definition;

import com.dfi.sbc2ha.helper.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MqttConfig {

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
    String topicPrefix = Constants.BONEIO;

    @JsonProperty("ha_discovery")
    HaDiscoveryConfig haDiscovery = new HaDiscoveryConfig();
}
