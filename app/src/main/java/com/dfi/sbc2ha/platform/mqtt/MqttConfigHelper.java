package com.dfi.sbc2ha.platform.mqtt;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.helper.ha.autodiscovery.message.Availability;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.dfi.sbc2ha.helper.ha.autodiscovery.message.Availability.formatTopic;

public class MqttConfigHelper {
    private final List<String> autodiscovery = new ArrayList<>();

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

    public String getSelfStateTopic() {
        return formatTopic(topicPrefix, Availability.STATE);
    }

    public String getSelfTopic() {
        return formatTopic(topicPrefix, "#");
    }

    public void addAutodiscovery(String topic) {

        autodiscovery.add(topic);
    }

    public List<String> getAutodiscovery() {
        return autodiscovery;
    }


}
