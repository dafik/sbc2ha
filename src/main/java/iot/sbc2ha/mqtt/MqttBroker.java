package iot.sbc2ha.mqtt;

import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight MQTT broker connection manager.
 *
 * <p>Handles lifecycle state transitions:
 * {@code MQTT_CONNECTING} → {@code MQTT_CONNECTED} (on success)
 * or {@code DEGRADED} (on failure).
 *
 * <p>Always fail-open: connection errors are logged and the application
 * continues in {@link LifecycleState#OFFLINE_READY} state.
 */
public final class MqttBroker {

    private static final Logger log = LoggerFactory.getLogger(MqttBroker.class);

    private final MqttConfig config;
    private final Lifecycle lifecycle;
    private final Object lifecycleLock = new Object();

    private IMqttAsyncClient client;
    private volatile boolean connected = false;

    /**
     * @param config    the MQTT configuration
     * @param lifecycle the boot lifecycle manager
     */
    public MqttBroker(MqttConfig config, Lifecycle lifecycle) {
        this.config = config;
        this.lifecycle = lifecycle;
    }

    /**
     * Attempt to connect to the MQTT broker.
     *
     * <p>Transitions the lifecycle:
     * <ul>
     *   <li>CONNECTING → CONNECTED on success</li>
     *   <li>CONNECTING → DEGRADED on failure</li>
     * </ul>
     *
     * <p>If MQTT is disabled, this is a no-op (skips transitions).
     */
    public void connect() {
        if (!config.isEnabled()) {
            log.info("MQTT disabled in configuration — skipping connection");
            return;
        }

        String brokerUri = buildBrokerUri();
        if (brokerUri == null) {
            log.warn("MQTT enabled but no broker URI configured — skipping connection");
            return;
        }

        String clientId = config.getClientId() != null
                ? config.getClientId()
                : "sbc2ha-" + Integer.toHexString((int) (Runtime.getRuntime().freeMemory() % 0x10000));

        try {
            synchronized (lifecycleLock) {
                client = new MqttAsyncClient(brokerUri, clientId, null);

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(config.isCleanSession());
                options.setKeepAliveInterval(config.getKeepAlive());
                options.setConnectionTimeout(config.getConnectionTimeout());

                if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                    options.setUserName(config.getUsername());
                }
                if (config.getPassword() != null) {
                    options.setPassword(config.getPassword().toCharArray());
                }

                log.info("Connecting to MQTT broker: {} (clientId={})", brokerUri, clientId);
                transition(LifecycleState.MQTT_CONNECTING);

                IMqttToken token = client.connect(options);
                token.waitForCompletion(10000);
                connected = true;

                log.info("Connected to MQTT broker: {}", brokerUri);
                transition(LifecycleState.MQTT_CONNECTED);
            }
        } catch (MqttException e) {
            log.warn("MQTT connection failed (non-fatal): {} — running offline", e.getMessage());
            transition(LifecycleState.DEGRADED);
        }
    }

    /**
     * @return {@code true} if currently connected to the broker
     */
    public boolean isConnected() {
        synchronized (lifecycleLock) {
            return connected && client != null && client.isConnected();
        }
    }

    /**
     * Publish a message to the given topic.
     *
     * <p>No-op if not connected. Errors are logged but never thrown.
     *
     * @param topic   the MQTT topic
     * @param payload the message payload bytes
     */
    public void publish(String topic, byte[] payload) {
        if (!isConnected() || client == null) {
            log.debug("MQTT not connected — dropping publish to topic: {}", topic);
            return;
        }
        try {
            MqttMessage message = new MqttMessage(payload);
            message.setQos(config.getQos());
            client.publish(topic, message, null, null).waitForCompletion(5000);
        } catch (MqttException e) {
            log.warn("MQTT publish failed: topic={}, error={}", topic, e.getMessage());
        }
    }

    /**
     * Disconnect and clean up the MQTT client.
     */
    public void shutdown() {
        synchronized (lifecycleLock) {
            if (client != null) {
                try {
                    IMqttToken disconnectToken = client.disconnect(5000);
                    disconnectToken.waitForCompletion(5000);
                    client.close();
                    log.info("MQTT broker disconnected");
                } catch (MqttException e) {
                    log.warn("MQTT disconnect failed: {}", e.getMessage());
                } finally {
                    client = null;
                    connected = false;
                }
            }
        }
    }

    // --- private helpers ---

    private String buildBrokerUri() {
        if (config.getBroker() != null && !config.getBroker().isEmpty()) {
            return config.getBroker();
        }
        return "tcp://localhost:" + config.getPort();
    }

    /**
     * Delegate to lifecycle for display + state transition.
     */
    private void transition(LifecycleState state) {
        if (lifecycle != null) {
            lifecycle.transition(state);
        }
    }
}
