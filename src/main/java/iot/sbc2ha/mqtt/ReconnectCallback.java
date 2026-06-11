package iot.sbc2ha.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Paho callback that triggers automatic reconnect on connection loss.
 *
 * <p>When the broker becomes unavailable, Paho invokes {@link #connectionLost},
 * which schedules a delayed reconnect with exponential backoff via
 * {@link MqttBroker#scheduleReconnect()}. The reconnect timer re-establishes
 * the client and re-subscribes to all previously registered topics.
 */
final class ReconnectCallback implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(ReconnectCallback.class);

    private final MqttBroker broker;

    /**
     * @param broker the owning MqttBroker instance
     */
    ReconnectCallback(MqttBroker broker) {
        this.broker = broker;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Handled by MqttBroker.subscribe() via the per-subscription callback.
        // This method exists only to satisfy the MqttCallback interface.
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // QoS 1/2 delivery confirmation — not used for durable state buffering.
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {} — scheduling reconnect", cause.getMessage());
        broker.scheduleReconnect();
    }
}
