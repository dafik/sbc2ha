package iot.sbc2ha.hardware.io.diozero;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BBB header-pint pin name parsing.
 *
 * <p>Verifies that pin identifiers in BBB header-pint notation (P8_37, P9_42)
 * match the expected pattern, and that non-matching names (kernel names,
 * malformed strings) are correctly rejected.</p>
 */
class DiozeroInputAdapterPinParsingTest {

    private static final Pattern HEADER_PIN_PATTERN = Pattern.compile("^(P[89])_(\\d+)$");

    @Test
    void patternMatchesP8Pins() {
        assertMatches("P8_03", "P8", "03");
        assertMatches("P8_37", "P8", "37");
        assertMatches("P8_46", "P8", "46");
    }

    @Test
    void patternMatchesP9Pins() {
        assertMatches("P9_11", "P9", "11");
        assertMatches("P9_42", "P9", "42");
        assertMatches("P9_14", "P9", "14");
    }

    @Test
    void patternRejectsInvalidHeaders() {
        assertDoesNotMatch("P7_37");
        assertDoesNotMatch("P10_37");
        assertDoesNotMatch("P0_37");
    }

    @Test
    void patternRejectsNonHeaderNotation() {
        assertDoesNotMatch("LCD_DATA8");
        assertDoesNotMatch("GPMC_AD6");
        assertDoesNotMatch("ECAP0_IN_PWM0_OUT");
    }

    @Test
    void patternRejectsDashSeparated() {
        assertDoesNotMatch("P8-37");
        assertDoesNotMatch("P9-42");
    }

    @Test
    void patternRejectsEmptyAndWhitespace() {
        assertDoesNotMatch("");
        assertDoesNotMatch("   ");
    }

    private void assertMatches(String pinId, String expectedHeader, String expectedPin) {
        Matcher m = HEADER_PIN_PATTERN.matcher(pinId);
        assertTrue(m.matches(), "Expected " + pinId + " to match pattern");
        assertEquals(expectedHeader, m.group(1));
        assertEquals(expectedPin, m.group(2));
    }

    private void assertDoesNotMatch(String pinId) {
        assertFalse(HEADER_PIN_PATTERN.matcher(pinId).matches(),
                "Expected " + pinId + " to NOT match pattern");
    }
}
