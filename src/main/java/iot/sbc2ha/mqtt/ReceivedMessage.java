package iot.sbc2ha.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Wrapper around a received MQTT message.
 *
 * <p>Provides access to the topic and the raw payload bytes, with convenience
 * methods for common content types.
 */
public final class ReceivedMessage {

    private final String topic;
    private final MqttMessage message;

    /**
     * @param topic   the MQTT topic this message arrived on
     * @param message the raw Paho MqttMessage
     */
    ReceivedMessage(String topic, MqttMessage message) {
        this.topic = topic;
        this.message = message;
    }

    /**
     * @return the MQTT topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the payload bytes
     */
    public byte[] getPayload() {
        return message.getPayload();
    }

    /**
     * @return the payload as a UTF-8 string
     */
    public String getPayloadString() {
        return message.toString();
    }

    /**
     * @return the QoS level of this message
     */
    public int getQos() {
        return message.getQos();
    }

    /**
     * @return whether this message has retain flag set
     */
    public boolean isRetained() {
        return message.isRetained();
    }
}
