package iot.sbc2ha.config;

/**
 * MQTT configuration — top-level {@code mqtt:} key.
 */
public final class MqttConfig {

    private boolean enabled;

    public MqttConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
