package iot.sbc2ha.hardware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HardwareMappingTest {

    @Test
    void createsWithAllFields() {
        PhysicalChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("btn_entrance", "Entrance button", ch);
        assertEquals("btn_entrance", mapping.logicalId());
        assertEquals("Entrance button", mapping.description());
        assertEquals(ch, mapping.physical());
    }

    @Test
    void equalsAndHashCode_sameContent() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping a = new HardwareMapping("btn_1", "Btn 1", ch);
        HardwareMapping b = new HardwareMapping("btn_1", "Btn 1", ch);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqual_differentLogicalId() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping a = new HardwareMapping("btn_1", "Btn 1", ch);
        HardwareMapping b = new HardwareMapping("btn_2", "Btn 1", ch);
        assertNotEquals(a, b);
    }

    @Test
    void notEqual_differentPhysical() {
        GpioChannel ch1 = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        GpioChannel ch2 = new GpioChannel("P9_12", GpioChannel.Direction.INPUT);
        HardwareMapping a = new HardwareMapping("btn_1", "Btn 1", ch1);
        HardwareMapping b = new HardwareMapping("btn_1", "Btn 1", ch2);
        assertNotEquals(a, b);
    }

    @Test
    void toStringIncludesAllFields() {
        GpioChannel ch = new GpioChannel("P9_11", GpioChannel.Direction.INPUT);
        HardwareMapping mapping = new HardwareMapping("btn_1", "Btn 1", ch);
        String s = mapping.toString();
        assertTrue(s.contains("btn_1"));
        assertTrue(s.contains("Btn 1"));
        assertTrue(s.contains("P9_11"));
    }
}
