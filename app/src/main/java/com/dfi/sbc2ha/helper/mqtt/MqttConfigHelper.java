package com.dfi.sbc2ha.helper.mqtt;

import com.dfi.sbc2ha.config.sbc2ha.definition.MqttConfig;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttConfigHelper {


    private final boolean fetchOldDiscovery = false;
    private final boolean managerReady = false;
    private final Map<String, String> autodiscoveryMessages = new HashMap<>();

    private final Map<String, String> discoveredMessages = new HashMap<>();

    @Getter
    private final String topicPrefix;
    @Getter
    private final boolean haDiscovery;
    @Getter
    private final String haDiscoveryPrefix;

    public MqttConfigHelper(MqttConfig config) {
        topicPrefix = config.getTopicPrefix();
        haDiscovery = config.getHaDiscovery().isEnabled();
        haDiscoveryPrefix = config.getHaDiscovery().getTopicPrefix();

    }

    public String cmdTopicPrefix() {
        return topicPrefix + "/cmd/";
    }

    public MqttTopicFilter subscribeTopic() {
        return MqttTopicFilter.of(cmdTopicPrefix() + "+/+/#");
    }

    public MqttTopicFilter getHaStatusTopic() {
        return MqttTopicFilter.of("homeassistant/status");

    }

    public List<MqttTopicFilter> getHaDiscoveryTopics() {
        return List.of(
                MqttTopicFilter.of("homeassistant/switch/" + topicPrefix + "/#"),
                MqttTopicFilter.of("homeassistant/light/" + topicPrefix + "/#"),
                MqttTopicFilter.of("homeassistant/binary_sensor/" + topicPrefix + "/#"),
                MqttTopicFilter.of("homeassistant/sensor/" + topicPrefix + "/#"),
                MqttTopicFilter.of("homeassistant/cover/" + topicPrefix + "/#"),
                MqttTopicFilter.of("homeassistant/button/" + topicPrefix + "/#")
        );
    }


    public void addAutodiscoveryMsg(String topic, String payload) {

        autodiscoveryMessages.put(topic, payload);
    }

    public void addDiscoveredMessages(String topic, String payload) {
        discoveredMessages.put(topic, payload);
    }

    public Map<String, String> getAutodiscoveryMessages() {
        return autodiscoveryMessages;
    }

    public Map<String, String> getDiscoveredMessages() {
        return discoveredMessages;
    }
}
