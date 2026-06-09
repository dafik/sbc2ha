package iot.sbc2ha.hardware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Mcp23017ChannelTest {

    @Test
    void createsWithLocationPortAndPin() {
        Mcp23017Channel ch = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        assertEquals("i2c-1:0x20:A:0", ch.location());
        assertEquals(Mcp23017Channel.Port.A, ch.port());
        assertEquals(0, ch.pin());
        assertEquals(PhysicalChannel.ChannelType.MCP23017, ch.channelType());
    }

    @Test
    void portBAndDifferentPin() {
        Mcp23017Channel ch = new Mcp23017Channel("i2c-1:0x21:B:7", Mcp23017Channel.Port.B, 7);
        assertEquals(Mcp23017Channel.Port.B, ch.port());
        assertEquals(7, ch.pin());
    }

    @Test
    void i2cAddressParsesFromLocation() {
        Mcp23017Channel ch = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        assertEquals(0x20, ch.i2cAddress());
    }

    @Test
    void i2cAddressParsesDecimal() {
        // 0x20 = 32 decimal
        Mcp23017Channel ch = new Mcp23017Channel("i2c-1:32:A:5", Mcp23017Channel.Port.A, 5);
        assertEquals(32, ch.i2cAddress());
    }

    @Test
    void equalsAndHashCode_sameLocation() {
        Mcp23017Channel a = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        Mcp23017Channel b = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentLocation() {
        Mcp23017Channel a = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        Mcp23017Channel b = new Mcp23017Channel("i2c-1:0x20:A:1", Mcp23017Channel.Port.A, 1);
        assertNotEquals(a, b);
    }

    @Test
    void notEqual_differentPort() {
        Mcp23017Channel a = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        Mcp23017Channel b = new Mcp23017Channel("i2c-1:0x20:B:0", Mcp23017Channel.Port.B, 0);
        assertNotEquals(a, b);
    }

    @Test
    void notEqual_differentPin() {
        Mcp23017Channel a = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        Mcp23017Channel b = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 1);
        assertNotEquals(a, b);
    }

    @Test
    void toStringIncludesAllFields() {
        Mcp23017Channel ch = new Mcp23017Channel("i2c-1:0x20:A:0", Mcp23017Channel.Port.A, 0);
        String s = ch.toString();
        assertTrue(s.contains("Mcp23017Channel"));
        assertTrue(s.contains("i2c-1:0x20:A:0"));
        assertTrue(s.contains("A"));
        assertTrue(s.contains("0"));
    }
}
