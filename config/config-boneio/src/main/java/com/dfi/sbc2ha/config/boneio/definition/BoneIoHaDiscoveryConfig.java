package com.dfi.sbc2ha.config.boneio.definition;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BoneIoHaDiscoveryConfig {

    boolean enabled = true;
    @JsonProperty("topic_prefix")
    String topicPrefix = "homeassistant";
}
