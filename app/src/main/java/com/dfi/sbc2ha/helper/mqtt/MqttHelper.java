package com.dfi.sbc2ha.helper.mqtt;

import com.dfi.sbc2ha.manager.Manager;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface MqttHelper {
    void start();

    void stop();


    void publish(String topic, byte[] payload);

    void publish(String topic, byte[] payload, boolean retain);

    Flowable<Mqtt5Publish> subscribeTopic(String topic);

    void createSubscription(String haDiscoveryTopic, Consumer<? super Mqtt5Publish> handle);

    Observable<Mqtt5Publish> getSubscriptions();

    boolean isReadyForPublish();

    void setConnectedListener(Manager connectedListener);

}
