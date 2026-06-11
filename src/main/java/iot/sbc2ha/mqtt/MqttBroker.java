package iot.sbc2ha.mqtt;

import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight MQTT broker connection manager with publish, subscribe,
 * and automatic reconnect.
 *
 * <p>Handles lifecycle state transitions:
 * {@code MQTT_CONNECTING} → {@code MQTT_CONNECTED} (on success)
 * or {@code DEGRADED} (on failure).
 *
 * <p>Always fail-open: connection errors are logged and the application
 * continues in {@link LifecycleState#OFFLINE_READY} state.
 *
 * <h3>Topic convention</h3>
 * <p>All topics are prefixed with {@code config.topicPrefix} (default
 * {@code sbc2ha}) so the full hierarchy is {@code sbc2ha/&lt;node_id&gt;/...}.
 * Callers should use {@link #buildTopic(String)} to prepend the prefix.
 */
public final class MqttBroker {

    private static final Logger log = LoggerFactory.getLogger(MqttBroker.class);

    /** Default topic prefix when none is configured. */
    private static final String DEFAULT_TOPIC_PREFIX = "sbc2ha";

    /** Initial reconnect delay (ms). */
    private static final long RECONNECT_DELAY_MS = 1000;

    /** Maximum reconnect delay (ms). */
    private static final long MAX_RECONNECT_DELAY_MS = 60000;

    /** Backoff multiplier. */
    private static final double RECONNECT_BACKOFF_MULTIPLIER = 1.5;

    private final MqttConfig config;
    private final Lifecycle lifecycle;
    private final Object lifecycleLock = new Object();

    private IMqttAsyncClient client;
    private volatile boolean connected = false;

    /** Exponential backoff delay for reconnect, reset on success. */
    private long reconnectDelayMs;

    /** Tracks subscriptions for re-subscribe after reconnect. */
    private final SortedMap<String, Integer> subscriptions = new ConcurrentSkipListMap<>();

    /** Scheduled executor for reconnect timer. Daemon thread, single-threaded. */
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sbc2ha-mqtt-reconnect");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> reconnectFuture;

    /**
     * @param config    the MQTT configuration
     * @param lifecycle the boot lifecycle manager
     */
    public MqttBroker(MqttConfig config, Lifecycle lifecycle) {
        this.config = config;
        this.lifecycle = lifecycle;
        this.reconnectDelayMs = RECONNECT_DELAY_MS;
    }

    // --- Connect / Disconnect ---

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
                MemoryPersistence persistence = new MemoryPersistence();
                client = new MqttAsyncClient(brokerUri, clientId, persistence);

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

                // Set callback for connection loss → auto-reconnect
                MqttCallback callback = new ReconnectCallback(this);
                client.setCallback(callback);

                log.info("Connecting to MQTT broker: {} (clientId={})", brokerUri, clientId);
                transition(LifecycleState.MQTT_CONNECTING);

                IMqttToken token = client.connect(options);
                token.waitForCompletion(10000);
                connected = true;
                reconnectDelayMs = RECONNECT_DELAY_MS;

                log.info("Connected to MQTT broker: {}", brokerUri);
                transition(LifecycleState.MQTT_CONNECTED);
            }
        } catch (MqttException e) {
            log.warn("MQTT connection failed (non-fatal): {} — running offline", e.getMessage());
            transition(LifecycleState.DEGRADED);
        }
    }

    /**
     * Disconnect and clean up the MQTT client.
     */
    public void shutdown() {
        cancelReconnectTimer();
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
        reconnectExecutor.shutdownNow();
    }

    // --- Publish / Subscribe ---

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
     * @param topic   the full MQTT topic (including prefix)
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
     * Subscribe to a topic and deliver messages via the given callback.
     *
     * <p>No-op if not connected. Errors are logged but never thrown.
     * Subscriptions are automatically re-established after reconnect.
     *
     * @param topic      the MQTT topic pattern (including prefix), e.g. {@code sbc2ha/<node_id>/control}
     * @param qos        the QoS level for this subscription
     * @param callback   invoked for each received message; invoked on the Paho callback thread
     */
    public void subscribe(String topic, int qos, MqttMessageCallback callback) {
        // Track subscription for re-subscribe after reconnect
        synchronized (subscriptions) {
            subscriptions.put(topic, qos);
        }

        if (!isConnected() || client == null) {
            log.debug("MQTT not connected — deferring subscribe to topic: {} (qos={})", topic, qos);
            return;
        }
        try {
            client.subscribe(topic, qos, (t, message) -> {
                try {
                    callback.onMessage(new ReceivedMessage(t, message));
                } catch (Exception e) {
                    log.warn("Message callback threw exception for topic {}: {}", t, e.getMessage());
                }
            });
            log.debug("Subscribed to topic: {} (qos={})", topic, qos);
        } catch (MqttException e) {
            log.warn("MQTT subscribe failed: topic={}, qos={}, error={}", topic, qos, e.getMessage());
        }
    }

    /**
     * Re-publish all registered subscriptions.
     *
     * <p>Called internally by {@link ReconnectCallback} when connection is restored.
     */
    void resubscribeAll() {
        SortedMap<String, Integer> subs;
        synchronized (subscriptions) {
            subs = new ConcurrentSkipListMap<>(subscriptions);
        }
        if (subs.isEmpty() || !isConnected() || client == null) {
            return;
        }
        for (var entry : subs.entrySet()) {
            try {
                client.subscribe(entry.getKey(), entry.getValue());
                log.debug("Re-subscribed to topic: {} (qos={})", entry.getKey(), entry.getValue());
            } catch (MqttException e) {
                log.warn("Re-subscribe failed after reconnect: topic={}, qos={}, error={}",
                        entry.getKey(), entry.getValue(), e.getMessage());
            }
        }
    }

    // --- Topic helpers ---

    /**
     * Prepend the configured topic prefix to a topic string.
     *
     * <p>If the topic already starts with the prefix, it is returned unchanged.
     * If {@code config.topicPrefix} is null or empty, defaults to {@code sbc2ha}.
     *
     * @param topic the topic suffix (e.g. {@code node_id/status})
     * @return the full prefixed topic (e.g. {@code sbc2ha/node_id/status})
     */
    public String buildTopic(String topic) {
        String prefix = config.getTopicPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_TOPIC_PREFIX;
        }
        if (topic.startsWith(prefix + "/")) {
            return topic;
        }
        return prefix + "/" + topic;
    }

    /**
     * @return the configured topic prefix, or the default {@code sbc2ha}.
     */
    public String getTopicPrefix() {
        String prefix = config.getTopicPrefix();
        return prefix != null && !prefix.isEmpty() ? prefix : DEFAULT_TOPIC_PREFIX;
    }

    // --- Private: reconnect timer ---

    /**
     * Schedule a reconnect attempt. Exponential backoff from reconnectDelayMs.
     */
    void scheduleReconnect() {
        cancelReconnectTimer();
        long delay = reconnectDelayMs;
        reconnectDelayMs = Math.min(
                (long) (reconnectDelayMs * RECONNECT_BACKOFF_MULTIPLIER),
                MAX_RECONNECT_DELAY_MS);
        log.info("Scheduling reconnect in {} ms (next: {} ms)", delay, reconnectDelayMs);
        reconnectFuture = reconnectExecutor.schedule(this::attemptReconnect, delay, TimeUnit.MILLISECONDS);
    }

    private void cancelReconnectTimer() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }

    /**
     * Called on successful reconnect to reset backoff.
     */
    void onReconnectSuccess() {
        reconnectDelayMs = RECONNECT_DELAY_MS;
    }

    // --- Private: reconnect attempt ---

    /**
     * Attempt to reconnect. Called by reconnect timer or ReconnectCallback.
     */
    void attemptReconnect() {
        String brokerUri = buildBrokerUri();
        if (brokerUri == null) {
            return;
        }

        String clientId = config.getClientId() != null
                ? config.getClientId()
                : "sbc2ha-" + Integer.toHexString((int) (Runtime.getRuntime().freeMemory() % 0x10000));

        try {
            synchronized (lifecycleLock) {
                MemoryPersistence persistence = new MemoryPersistence();
                client = new MqttAsyncClient(brokerUri, clientId, persistence);

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

                MqttCallback callback = new ReconnectCallback(this);
                client.setCallback(callback);

                IMqttToken token = client.connect(options);
                token.waitForCompletion(10000);
                connected = true;
                onReconnectSuccess();

                log.info("Reconnected to MQTT broker: {}", brokerUri);

                // Re-publish all subscriptions
                resubscribeAll();
            }
        } catch (MqttException e) {
            log.warn("MQTT reconnect failed: {} — scheduling next attempt", e.getMessage());
            scheduleReconnect();
        }
    }

    // --- Private helpers ---

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
