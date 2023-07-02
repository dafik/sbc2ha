package com.dfi.sbc2ha.config.sbc2ha.definition;

import com.dfi.sbc2ha.helper.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HaDiscoveryConfig {

    boolean enabled = true;
    @JsonProperty("topic_prefix")
    String topicPrefix = Constants.HOMEASSISTANT;
}
