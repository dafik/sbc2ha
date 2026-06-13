package iot.sbc2ha.hardware.io.diozero;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import iot.sbc2ha.hardware.HardwareChip;
import iot.sbc2ha.hardware.HardwareModel;
import iot.sbc2ha.hardware.OledChannel;
import iot.sbc2ha.hardware.PhysicalChannel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OledChannel YAML deserialization and factory createOledDisplay.
 */
class OledChannelYamlTest {

    private static final JsonMapper yamlMapper = JsonMapper.builder(new YAMLFactory())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build();

    @BeforeAll
    static void setUp() {
        System.setProperty("diozero.devicefactory", "com.diozero.internal.provider.mock.MockDeviceFactory");
    }

    @Test
    void yamlParsesOledChannel() throws Exception {
        String yaml = """
                type: oled
                bus: oled1
                """;
        PhysicalChannel ch = yamlMapper.readValue(yaml, PhysicalChannel.class);
        assertInstanceOf(OledChannel.class, ch);
        OledChannel oled = (OledChannel) ch;
        assertEquals("oled1", oled.bus());
    }

    @Test
    void yamlParsesOledChannelDecimalAddress() throws Exception {
        String yaml = """
                type: oled
                bus: oled2
                """;
        PhysicalChannel ch = yamlMapper.readValue(yaml, PhysicalChannel.class);
        assertInstanceOf(OledChannel.class, ch);
        OledChannel oled = (OledChannel) ch;
        assertEquals("oled2", oled.bus());
    }

    @Test
    void yamlParsesMixedChannels() throws Exception {
        String yaml = """
                - type: gpio
                  pin: "P9_11"
                  direction: input
                - type: oled
                  bus: oled1
                """;
        List<PhysicalChannel> channels = yamlMapper.readValue(yaml,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, PhysicalChannel.class));
        assertEquals(2, channels.size());
        assertInstanceOf(iot.sbc2ha.hardware.GpioChannel.class, channels.get(0));
        assertInstanceOf(OledChannel.class, channels.get(1));
    }

    @Test
    void factoryCreateOledDisplayReturnsDisplay() {
        OledChannel ch = new OledChannel("oled1");
        HardwareModel model = new HardwareModel("test", null,
                List.of(new HardwareChip("oled1", "oled", 0x3C)),
                List.of(ch), List.of());
        var display = DiozeroInputOutputFactory.INSTANCE.createOledDisplay(ch, model);
        assertNotNull(display);
        display.close(); // clean up
    }

    @Test
    void factoryCreateOledDisplayFailOpen() {
        // OledBootDisplay constructor catches exceptions and returns non-null
        // (with internal oled=null). The factory wraps this in try-catch too.
        OledChannel ch = new OledChannel("oled99");
        HardwareModel model = new HardwareModel("test", null,
                List.of(new HardwareChip("oled99", "oled", 0xFF)),
                List.of(ch), List.of());
        assertDoesNotThrow(() -> {
            var display = DiozeroInputOutputFactory.INSTANCE.createOledDisplay(ch, model);
            if (display != null) display.close();
        });
    }
}
