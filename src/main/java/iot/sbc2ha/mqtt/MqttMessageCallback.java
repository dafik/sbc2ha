package iot.sbc2ha.mqtt;

/**
 * Callback interface for MQTT subscriptions.
 *
 * <p>Implementations must not throw exceptions — any error is caught
 * and logged by {@link iot.sbc2ha.mqtt.MqttBroker#subscribe(String, int, MqttMessageCallback)}.
 */
@FunctionalInterface
public interface MqttMessageCallback {

    /**
     * Invoked when a message arrives on a subscribed topic.
     *
     * @param message the received message, including topic and payload
     */
    void onMessage(ReceivedMessage message);
}
