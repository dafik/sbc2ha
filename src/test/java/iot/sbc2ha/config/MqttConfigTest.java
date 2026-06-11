package iot.sbc2ha.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MqttConfigTest {

    @Test
    void defaults() {
        MqttConfig cfg = new MqttConfig();
        assertFalse(cfg.isEnabled());
        assertNull(cfg.getBroker());
        assertEquals(1883, cfg.getPort());
        assertNull(cfg.getUsername());
        assertNull(cfg.getPassword());
        assertNull(cfg.getClientId());
        assertNull(cfg.getTopicPrefix());
        assertEquals(0, cfg.getQos());
        assertTrue(cfg.isCleanSession());
        assertEquals(60, cfg.getKeepAlive());
        assertEquals(10, cfg.getConnectionTimeout());
    }

    @Test
    @SuppressWarnings("all") // getter-on-fresh-value is trivially true/false
    void setters() {
        MqttConfig cfg = new MqttConfig();
        cfg.setEnabled(true);
        cfg.setBroker("tcp://broker.example.com:1883");
        cfg.setPort(8883);
        cfg.setUsername("admin");
        cfg.setPassword("secret");
        cfg.setClientId("sbc-test-1");
        cfg.setTopicPrefix("sbc2ha");
        cfg.setQos(1);
        cfg.setCleanSession(false);
        cfg.setKeepAlive(120);
        cfg.setConnectionTimeout(30);

        assertTrue(cfg.isEnabled());
        assertEquals("tcp://broker.example.com:1883", cfg.getBroker());
        assertEquals(8883, cfg.getPort());
        assertEquals("admin", cfg.getUsername());
        assertEquals("secret", cfg.getPassword());
        assertEquals("sbc-test-1", cfg.getClientId());
        assertEquals("sbc2ha", cfg.getTopicPrefix());
        assertEquals(1, cfg.getQos());
        assertFalse(cfg.isCleanSession());
        assertEquals(120, cfg.getKeepAlive());
        assertEquals(30, cfg.getConnectionTimeout());

        // Verify disabled config has default values (no broker = null port default unused)
        MqttConfig disabled = new MqttConfig();
        disabled.setEnabled(false);
        assertEquals(1883, disabled.getPort());
    }

    @Test
    void defaultsAllowOfflineFirst() {
        // enabled=false and null broker mean no connection attempt is ever made
        MqttConfig cfg = new MqttConfig();
        assertFalse(cfg.isEnabled());
        assertNull(cfg.getBroker());
        assertNotEquals(false, cfg.isCleanSession());
    }
}
