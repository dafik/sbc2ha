package com.dfi.sbc2ha.config.boneio.definition.action;

import com.dfi.sbc2ha.config.boneio.definition.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoMqttActionConfig extends BoneIoActionConfig {

    String topic;

    @JsonProperty("action_mqtt_msg")
    String actionMqttMsg;

    public BoneIoMqttActionConfig() {
        super();
        action = ActionType.MQTT;
    }
}
