package iot.sbc2ha.config;

/**
 * MQTT configuration — top-level {@code mqtt:} key.
 *
 * <p>All fields default to sensible offline-first values so that
 * omitting optional fields never causes a connection attempt.</p>
 */
public final class MqttConfig {

    /** Whether MQTT is enabled in this configuration. */
    private boolean enabled;

    /** Broker URI, e.g. {@code tcp://localhost:1883}. */
    private String broker;

    /** Broker port — defaults to 1883 when unset. */
    private int port = 1883;

    /** Optional username for broker authentication. */
    private String username;

    /** Optional password for broker authentication. */
    private String password;

    /** MQTT client identifier. Generated if unset. */
    private String clientId;

    /** Base topic prefix, e.g. {@code sbc2ha}. */
    private String topicPrefix;

    /** Default QoS level (0, 1, or 2). Defaults to 0. */
    private int qos = 0;

    /** Clean session flag. Defaults to true. */
    private boolean cleanSession = true;

    /** Keep-alive interval in seconds. Defaults to 60. */
    private int keepAlive = 60;

    /** Connection timeout in seconds. Defaults to 10. */
    private int connectionTimeout = 10;

    public MqttConfig() {
    }

    // --- getters / setters (for Jackson deserialization) ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
