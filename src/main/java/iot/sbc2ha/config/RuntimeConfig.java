package iot.sbc2ha.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Runtime configuration — top-level {@code runtime:} key.
 */
@SuppressWarnings("unused")
public final class RuntimeConfig {

    @JsonProperty("offline_first")
    private boolean offlineFirst;

    public RuntimeConfig() {
    }

    public boolean isOfflineFirst() {
        return offlineFirst;
    }

    @SuppressWarnings("unused")
    public void setOfflineFirst(boolean offlineFirst) {
        this.offlineFirst = offlineFirst;
    }
}
