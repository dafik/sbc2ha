package iot.sbc2ha.hardware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OledChannelTest {

    @Test
    void createsWithBus() {
        OledChannel ch = new OledChannel("oled1");
        assertEquals("oled1", ch.bus());
        assertEquals(PhysicalChannel.ChannelType.OLED, ch.channelType());
    }

    @Test
    void differentBus() {
        OledChannel ch = new OledChannel("oled2");
        assertEquals("oled2", ch.bus());
    }

    @Test
    void equalsAndHashCode_sameBus() {
        OledChannel a = new OledChannel("oled1");
        OledChannel b = new OledChannel("oled1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentBus() {
        OledChannel a = new OledChannel("oled1");
        OledChannel b = new OledChannel("oled2");
        assertNotEquals(a, b);
    }

    @Test
    void toStringIncludesBus() {
        OledChannel ch = new OledChannel("oled1");
        String s = ch.toString();
        assertTrue(s.contains("OledChannel"));
        assertTrue(s.contains("oled1"));
    }
}
