package com.dfi.sbc2ha.platform.mqtt;

import io.reactivex.functions.BiConsumer;

public interface MqttHelper {
    void start();

    void stop();

    default void publish(String topic, byte[] payload){
        publish(topic, payload, true);
    }

    void publish(String topic, byte[] payload, boolean retain);

    void createSubscription(String haDiscoveryTopic, BiConsumer<String, byte[]> handle);

    void removeSubscription(String haDiscoveryTopic);

    boolean isReadyForPublish();
}
