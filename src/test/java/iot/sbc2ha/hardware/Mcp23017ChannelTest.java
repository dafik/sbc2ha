package iot.sbc2ha.hardware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Mcp23017ChannelTest {

    @Test
    void createsWithBusAndPin() {
        Mcp23017Channel ch = new Mcp23017Channel("mcp1", 0);
        assertEquals("mcp1", ch.bus());
        assertEquals(0, ch.pin());
        assertEquals(0, ch.portAPin());
        assertEquals(-1, ch.portBPin());
        assertEquals(PhysicalChannel.ChannelType.MCP23017, ch.channelType());
    }

    @Test
    void portBAndDifferentPin() {
        Mcp23017Channel ch = new Mcp23017Channel("mcp2", 10);
        assertEquals(10, ch.pin());
        assertEquals(-1, ch.portAPin());
        assertEquals(2, ch.portBPin());
    }

    @Test
    void equalsAndHashCode_sameBusPin() {
        Mcp23017Channel a = new Mcp23017Channel("mcp1", 0);
        Mcp23017Channel b = new Mcp23017Channel("mcp1", 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentBus() {
        Mcp23017Channel a = new Mcp23017Channel("mcp1", 0);
        Mcp23017Channel b = new Mcp23017Channel("mcp2", 0);
        assertNotEquals(a, b);
    }

    @Test
    void notEqual_differentPin() {
        Mcp23017Channel a = new Mcp23017Channel("mcp1", 0);
        Mcp23017Channel b = new Mcp23017Channel("mcp1", 1);
        assertNotEquals(a, b);
    }

    @Test
    void toStringIncludesAllFields() {
        Mcp23017Channel ch = new Mcp23017Channel("mcp1", 0);
        String s = ch.toString();
        assertTrue(s.contains("Mcp23017Channel"));
        assertTrue(s.contains("mcp1"));
        assertTrue(s.contains("0"));
    }
}
