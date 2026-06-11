package iot.sbc2ha.mqtt;

import iot.sbc2ha.boot.Lifecycle;
import iot.sbc2ha.boot.LifecycleState;
import iot.sbc2ha.config.MqttConfig;
import iot.sbc2ha.boot.BootDisplay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MqttBrokerTest {

    private Lifecycle lifecycle;
    private List<LifecycleState> transitions;

    @BeforeEach
    void setUp() {
        transitions = new ArrayList<>();
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
        lifecycle.transition(LifecycleState.OFFLINE_READY);
    }

    // --- Existing tests (unchanged) ---

    @Test
    void disabledMqtt_noConnectionAttempt() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(broker::connect);
        assertFalse(broker.isConnected());

        assertEquals(1, transitions.size());
        assertEquals(LifecycleState.OFFLINE_READY, transitions.get(0));
    }

    @Test
    void unreachableBroker_failsGracefully() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(true);
        cfg.setBroker("tcp://127.0.0.1:19999");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(broker::connect);
        assertFalse(broker.isConnected());

        assertTrue(transitions.contains(LifecycleState.MQTT_CONNECTING));
        assertTrue(transitions.contains(LifecycleState.DEGRADED));
    }

    @Test
    void publish_notConnected_noException() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(() -> broker.publish("test/topic", "hello".getBytes()));
    }

    @Test
    void close_neverConnected_noException() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(broker::shutdown);
    }

    // --- New tests for SBC-023 ---

    @Test
    void subscribe_disabled_noException() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        boolean[] called = {false};
        assertDoesNotThrow(() -> broker.subscribe("test/topic", 0, msg -> called[0] = true));
        assertFalse(called[0]);
    }

    @Test
    void subscribe_notConnected_defersSilently() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(true);
        cfg.setBroker("tcp://127.0.0.1:19998"); // won't connect

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        broker.connect();
        assertFalse(broker.isConnected());

        boolean[] called = {false};
        // Should not throw, not call callback (no broker to deliver)
        assertDoesNotThrow(() -> broker.subscribe("test/topic", 0, msg -> called[0] = true));
        assertFalse(called[0]);
    }

    @Test
    void buildTopic_withPrefix() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);
        cfg.setTopicPrefix("myco");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("myco/node_id/status", broker.buildTopic("node_id/status"));
    }

    @Test
    void buildTopic_defaultPrefix() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("sbc2ha/node_id/status", broker.buildTopic("node_id/status"));
    }

    @Test
    void buildTopic_alreadyPrefixed_returnedUnchanged() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);
        cfg.setTopicPrefix("sbc2ha");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        // Already starts with "sbc2ha/" — should be returned unchanged
        assertEquals("sbc2ha/existing/topic", broker.buildTopic("sbc2ha/existing/topic"));
    }

    @Test
    void buildTopic_differentPrefix_notDoubled() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);
        cfg.setTopicPrefix("other");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        // Starts with "other/" — returned unchanged
        assertEquals("other/existing", broker.buildTopic("other/existing"));
        // Doesn't start with "other/" — gets prefixed
        assertEquals("other/new/topic", broker.buildTopic("new/topic"));
    }

    @Test
    void getTopicPrefix_custom() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);
        cfg.setTopicPrefix("myco");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("myco", broker.getTopicPrefix());
    }

    @Test
    void getTopicPrefix_default() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("sbc2ha", broker.getTopicPrefix());
    }

    @Test
    void getTopicPrefix_nullConfigUsesDefault() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);
        cfg.setTopicPrefix(null);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("sbc2ha", broker.getTopicPrefix());
    }

    @Test
    void getTopicPrefix_emptyConfigUsesDefault() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);
        cfg.setTopicPrefix("");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("sbc2ha", broker.getTopicPrefix());
    }

    @Test
    void publish_connected_butBrokerUnavailable_dropsSilently() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(true);
        cfg.setBroker("tcp://127.0.0.1:19997");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        broker.connect();
        assertFalse(broker.isConnected());

        // Even with connected=false, publish should not throw
        assertDoesNotThrow(() -> broker.publish("any/topic", "payload".getBytes()));
    }

    @Test
    void shutdown_alreadyShutdown_noException() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        broker.shutdown();
        assertDoesNotThrow(broker::shutdown);
    }

    @Test
    void connect_twice_firstFails_secondAlsoFailsGracefully() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(true);
        cfg.setBroker("tcp://127.0.0.1:19996");

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertDoesNotThrow(broker::connect);
        assertFalse(broker.isConnected());

        // Second connect should also not throw
        assertDoesNotThrow(broker::connect);
        assertFalse(broker.isConnected());
    }

    @Test
    void buildTopic_simpleKey() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        assertEquals("sbc2ha/availability", broker.buildTopic("availability"));
        assertEquals("sbc2ha/control", broker.buildTopic("control"));
    }

    @Test
    void messageCallback_exceptionCaught() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(false);

        MqttBroker broker = new MqttBroker(cfg, lifecycle);
        // Subscribe should not throw even if callback is a no-op
        assertDoesNotThrow(() -> broker.subscribe("test", 0, msg -> {
            throw new RuntimeException("intentional");
        }));
    }
}
