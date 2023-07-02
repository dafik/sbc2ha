package com.dfi.sbc2ha.config.sbc2ha.definition.action;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MqttActionConfig extends ActionConfig {

    String topic;

    @JsonProperty("action_mqtt_msg")
    String actionMqttMsg;

    public MqttActionConfig() {
        super();
        action = ActionType.MQTT;
    }
}
