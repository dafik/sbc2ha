package iot.sbc2ha.mqtt;

import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.MqttConfig;
import iot.sbc2ha.boot.BootDisplay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MqttBrokerTest {

    private Lifecycle lifecycle;
    private java.util.List<LifecycleState> transitions;

    @BeforeEach
    void setUp() {
        transitions = new java.util.ArrayList<>();
        BootDisplay fakeDisplay = new BootDisplay() {
            @Override
            public void update(LifecycleState state) {
                transitions.add(state);
            }
            @Override
            public void close() {
            }
        };
        lifecycle = new Lifecycle(fakeDisplay);
        // Manually set lifecycle to OFFLINE_READY (simulating that we reach this point
        // before MQTT connects in Main.start())
        lifecycle.transition(LifecycleState.OFFLINE_READY);
    }

    @Test
    void disabledMqtt_noConnectionAttempt() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(broker::connect);
        assertFalse(broker.isConnected());

        // No MQTT states should have been transitioned
        assertEquals(1, transitions.size());
        assertEquals(LifecycleState.OFFLINE_READY, transitions.getFirst());
    }

    @Test
    void unreachableBroker_failsGracefully() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(true);
        cfg.setBroker("tcp://127.0.0.1:19999"); // port that won't listen

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        // connect() should not throw — it's fail-open
        assertDoesNotThrow(broker::connect);
        assertFalse(broker.isConnected());

        // Should have transitioned through CONNECTING → DEGRADED
        assertTrue(transitions.contains(LifecycleState.MQTT_CONNECTING));
        assertTrue(transitions.contains(LifecycleState.DEGRADED));
    }

    @Test
    void publish_notConnected_noException() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        // Should not throw even if not connected
        assertDoesNotThrow(() -> broker.publish("test/topic", "hello".getBytes()));
    }

    @Test
    void close_neverConnected_noException() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(broker::shutdown);
    }
}
