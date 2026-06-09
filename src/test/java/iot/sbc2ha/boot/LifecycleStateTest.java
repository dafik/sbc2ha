package iot.sbc2ha.boot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleStateTest {

    @Test
    void allStatesPresent() {
        LifecycleState[] expected = {
            LifecycleState.BOOTING,
            LifecycleState.CONFIG_LOADED,
            LifecycleState.CONFIG_ERROR,
            LifecycleState.STATE_RESTORED,
            LifecycleState.HARDWARE_MINIMAL_READY,
            LifecycleState.OFFLINE_READY,
            LifecycleState.MQTT_CONNECTING,
            LifecycleState.MQTT_CONNECTED,
            LifecycleState.HA_DISCOVERY_RUNNING,
            LifecycleState.HA_DISCOVERY_DONE,
            LifecycleState.FULL_READY,
            LifecycleState.DEGRADED,
            LifecycleState.ERROR
        };
        assertArrayEquals(expected, LifecycleState.values());
    }

    @Test
    void configErrorDistinctFromError() {
        assertNotEquals(LifecycleState.CONFIG_ERROR, LifecycleState.ERROR);
    }

    @Test
    void offlineReadyDistinctFromFullReady() {
        assertNotEquals(LifecycleState.OFFLINE_READY, LifecycleState.FULL_READY);
    }
}
