package com.dfi.sbc2ha.helper.mqtt;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import lombok.Getter;

import java.util.HashMap;
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


    public String getCommandTopic() {
        return cmdTopicPrefix() + "+/+/#";
    }

    public String getHaStatusTopic() {
        return "homeassistant/status";
    }
    public String getHaDiscoveryTopic() {
        return "homeassistant/+/" + topicPrefix + "/#";
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
