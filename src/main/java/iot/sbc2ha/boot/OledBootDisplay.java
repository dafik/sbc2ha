package iot.sbc2ha.boot;

import com.diozero.api.I2CDevice;
import com.diozero.devices.oled.SH1106;
import com.diozero.devices.oled.SsdOledCommunicationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * SH1106-based I2C OLED BootDisplay (128x64).
 *
 * <p>Drives a standard 128x64 SH1106 OLED via I2C using diozero's
 * {@link SH1106}. Displays the current lifecycle state as centred text.</p>
 *
 * <h3>Fail-open behaviour</h3>
 * <p>Any failure during {@code update()} or {@code close()} is caught,
 * logged at WARN, and silently ignored — the boot sequence is never
 * blocked by a display failure.</p>
 *
 * <h3>Hardware notes</h3>
 * <p>Uses I2C bus 1 with the standard SH1106 address (0x3C).
 * For different configurations use the package-private constructor.</p>
 *
 * @see BootDisplay
 */
public final class OledBootDisplay implements BootDisplay {

    private static final Logger log = LoggerFactory.getLogger(OledBootDisplay.class);

    // Standard SH1106 I2C address
    static final int I2C_ADDRESS = 0x3C;

    private final SH1106 oled;

    /**
     * Create an OledBootDisplay on I2C bus 1 with the standard
     * SH1106 address (0x3C) and 128x64 size.
     */
    @SuppressWarnings("unused")
    public OledBootDisplay() {
        this(1, I2C_ADDRESS);
    }

    /**
     * Create an OledBootDisplay with explicit I2C bus and address.
     *
     * @param i2cBus  the I2C bus number
     * @param i2cAddr the I2C device address
     */
    OledBootDisplay(int i2cBus, int i2cAddr) {
        I2CDevice i2cDevice = null;
        SH1106 localOled = null;
        try {
            i2cDevice = new I2CDevice(i2cBus, i2cAddr);
            SsdOledCommunicationChannel channel =
                    new SsdOledCommunicationChannel.I2cCommunicationChannel(i2cDevice);
            // 128 width, Height.TALL = 64 lines
            localOled = new SH1106(channel, 128, SH1106.Height.TALL);
            log.info("OledBootDisplay initialised: bus={}, addr=0x{}", i2cBus,
                    Integer.toHexString(i2cAddr));
        } catch (Exception e) {
            log.warn("OledBootDisplay initialisation failed (non-fatal): {}", e.getMessage());
            if (i2cDevice != null) {
                try { i2cDevice.close(); } catch (Exception ignored) {}
            }
        }
        this.oled = localOled;
    }

    // -----------------------------------------------------------------------
    // BootDisplay
    // -----------------------------------------------------------------------

    @Override
    public void update(LifecycleState state) {
        try {
            if (oled == null) return;

            String label = state.name();
            Font font = oled.defaultFont(2);
            BufferedImage image = renderText(label, oled.getWidth(), oled.getHeight(), font);
            oled.display(image);
        } catch (Exception e) {
            log.warn("OledBootDisplay.update failed (non-fatal): {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (oled != null) {
                oled.close();
                log.info("OledBootDisplay closed");
            }
        } catch (Exception e) {
            log.warn("OledBootDisplay.close failed (non-fatal): {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Text rendering
    // -----------------------------------------------------------------------

    /**
     * Render text centred on a binary image suitable for {@link SH1106#display(BufferedImage)}.
     */
    static BufferedImage renderText(String text, int width, int height, Font font) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setColor(Color.WHITE);
        g2d.setFont(font);

        // Centre the text
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + fm.getAscent();

        g2d.drawString(text, x, y);
        g2d.dispose();

        return image;
    }
}
