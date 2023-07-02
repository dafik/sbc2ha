package com.dfi.sbc2ha.manager;

import com.hivemq.client.mqtt.datatypes.MqttTopic;
import lombok.Data;

@Data
public class ManagerCommandExternal implements ManagerCommand {
    private final MqttTopic topic;
    private final byte[] payloadAsBytes;

    public ManagerCommandExternal(MqttTopic topic, byte[] payloadAsBytes) {

        this.topic = topic;
        this.payloadAsBytes = payloadAsBytes;
    }
}
