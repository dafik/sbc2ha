package iot.sbc2ha.hardware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GpioChannelTest {

    @Test
    void createsWithPinAndDirection() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        assertEquals("P9_11", ch.pinLabel());
        assertEquals(GpioChannel.Direction.INPUT, ch.direction());
        assertEquals(PhysicalChannel.ChannelType.GPIO, ch.channelType());
    }

    @Test
    void outputDirection() {
        GpioChannel ch = new GpioChannel("P9_12", GpioChannel.Direction.OUTPUT);
        assertEquals(GpioChannel.Direction.OUTPUT, ch.direction());
    }

    @Test
    void equalsAndHashCode_sameLocation() {
        GpioChannel a = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel b = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentLocation() {
        GpioChannel a = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel b = new GpioChannel("P9_12", GpioChannel.Direction.INPUT);
        assertNotEquals(a, b);
    }

    @Test
    void notEqual_differentDirection() {
        GpioChannel a = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel b = new GpioChannel("P9_11", GpioChannel.Direction.OUTPUT);
        assertNotEquals(a, b);
    }

    @Test
    void toStringIncludesAllFields() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        String s = ch.toString();
        assertTrue(s.contains("GpioChannel"));
        assertTrue(s.contains("P9_11"));
        assertTrue(s.contains("INPUT"));
    }
}
