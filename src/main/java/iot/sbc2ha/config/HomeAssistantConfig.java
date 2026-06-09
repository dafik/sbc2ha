package iot.sbc2ha.config;

/**
 * Home Assistant configuration — top-level {@code home_assistant:} key.
 */
public final class HomeAssistantConfig {

    private boolean enabled;

    public HomeAssistantConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
