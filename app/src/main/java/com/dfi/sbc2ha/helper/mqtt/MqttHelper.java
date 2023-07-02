package com.dfi.sbc2ha.helper.mqtt;

import com.dfi.sbc2ha.manager.Manager;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Observable;

public interface MqttHelper {
    void start();

    void stop();


    void publish(String topic, byte[] payload);

    void publish(String topic, byte[] payload, boolean retain);

    Observable<Mqtt5Publish> getSubscriptions();

    boolean isReadyForPublish();

    void setConnectedListener(Manager connectedListener);
}
