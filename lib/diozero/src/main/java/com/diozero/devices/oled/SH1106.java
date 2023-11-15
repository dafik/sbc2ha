package com.diozero.devices.oled;


import com.diozero.api.DeviceInterface;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

@Slf4j
@SuppressWarnings("unused")
public class SH1106 implements DeviceInterface {
    public static final int WIDTH = 128;
    public static final byte DISPLAY_OFF = (byte) 0xAE;
    public static final byte DISPLAY_ON = (byte) 0xAF;
    // Fundamental commands
    private static final byte SET_CONTRAST = (byte) 0x81;
    private static final byte RESUME_TO_RAM_CONTENT_DISPLAY = (byte) 0xA4;
    private static final byte ENTIRE_DISPLAY_ON = (byte) 0xA5;
    private static final byte NORMAL_DISPLAY = (byte) 0xA6; // Default
    private static final byte INVERSE_DISPLAY = (byte) 0xA7;

    // Scrolling commands
    private static final byte RIGHT_HORIZONTAL_SCROLL = (byte) 0x26;
    private static final byte LEFT_HORIZONTAL_SCROLL = (byte) 0x27;
    private static final byte VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = (byte) 0x29;
    private static final byte VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = (byte) 0x2A;
    /**
     * After sending 2Eh command to deactivate the scrolling
     * action, the ram data needs to be rewritten.
     */
    private static final byte DEACTIVATE_SCROLL = (byte) 0x2E;
    private static final byte ACTIVATE_SCROLL = (byte) 0x2F;
    private static final byte SET_VERTICAL_SCROLL_AREA = (byte) 0xA3;

    // Addressing Setting Command Table
    private static final byte SET_LOWER_COLUMN_START_ADDR = (byte) 0x00; // For Page addressing mode
    private static final byte SET_HIGHER_COLUMN_START_ADDR = (byte) 0x10; // For Page addressing mode
    private static final byte SET_MEMORY_ADDR_MODE = (byte) 0x20;
    private static final byte SET_COLUMN_ADDR = (byte) 0x21; // For Horiz or Vertical addressing modes
    private static final byte SET_PAGE_ADDR = (byte) 0x22; // For Horiz or Vertical addressing modes
    private static final byte SET_PAGE_START_ADDR = (byte) 0xB0; // For Page addressing mode (0xB0-B7)

    // Hardware Configuration (Panel resolution & layout related) Command Table
    private static final byte SET_DISPLAY_START_LINE_0 = (byte) 0x40; // Set display start line from 0-63 (0x40-7F)
    // Column address 0 is mapped to SEG0
    private static final byte SET_SEGMENT_REMAP_OFF = (byte) 0xA0;
    // Column address 127 is mapped to SEG0
    private static final byte SET_SEGMENT_REMAP_ON = (byte) 0xA1;
    // Set MUX ratio to N+1 MUX. From 16MUX to 64MUX, RESET=111111b (i.e. 63d, 64MUX)
    private static final byte SET_MULTIPLEX_RATIO = (byte) 0xA8;
    // enable internal IREF during display on
    private static final byte SET_IREF_INTERNAL = (byte) 0xAD;
    // Normal mode. Scan from COM0 to COM[N ?1] (Default)
    private static final byte COM_OUTPUT_SCAN_DIR_NORMAL = (byte) 0xC0;
    // Remapped mode. Scan from COM[N-1] to COM0 (vertically flipped)
    private static final byte COM_OUTPUT_SCAN_DIR_REMAPPED = (byte) 0xC8;
    // Set vertical shift by COM from 0d~63d (Default = 0x00)
    private static final byte SET_DISPLAY_OFFSET = (byte) 0xD3;
    // COM Pins Hardware Configuration
    private static final byte SET_COM_PINS_HW_CONFIG = (byte) 0xDA;

    // Timing & Driving Scheme Setting Command Table
    // Set Display Clock Divide Ratio/Oscillator Frequency
    private static final byte DISPLAY_CLOCK_DIV_OSC_FREQ = (byte) 0xD5;
    // Set Pre-charge Period
    private static final byte SET_PRECHARGE_PERIOD = (byte) 0xD9;
    private static final byte SET_VCOMH_DESELECT_LEVEL = (byte) 0xDB;
    private static final byte PRECHARGE_PERIOD_EXTERNALVCC = 0x22;
    private static final byte PRECHARGE_PERIOD_SWITCHCAPVCC = (byte) 0xF1;

    // Charge Pump Command Table
    private static final byte SET_CHARGE_PUMP = (byte) 0x8D;
    private static final byte CHARGE_PUMP_DISABLED = 0x10;
    private static final byte CHARGE_PUMP_ENABLED = 0x14;

    private static final byte CONTRAST_EXTERNALVCC = (byte) 0x9F;
    private static final byte CONTRAST_SWITCHCAPVCC = (byte) 0xCF;

    // Memory addressing modes (SET_MEMORY_ADDR_MODE)
    private static final byte ADDR_MODE_HORIZ = 0b00; // Horizontal Addressing Mode
    private static final byte ADDR_MODE_VERT = 0b01;  // Vertical Addressing Mode
    private static final byte ADDR_MODE_PAGE = 0b10;  // Page Addressing Mode (RESET)

    // COM pins hardware config (SET_COM_PINS_HW_CONFIG)
    private static final byte COM_PINS_SEQUENTIAL_NO_REMAP = 0b0000_0010;
    private static final byte COM_PINS_ALT_NO_REMAP = 0b0001_0010;
    private static final byte COM_PINS_SEQUENTIAL_REMAP = 0b0010_0010;
    private static final byte COM_PINS_ALT_REMAP = 0b0011_0010;

    // Vcomh Deselect Levels (SET_VCOMH_DESELECT_LEVEL)
    private static final byte VCOMH_DESELECT_LEVEL_065 = 0b0000_0000; // 0.65 x VCC
    private static final byte VCOMH_DESELECT_LEVEL_077 = 0b0010_0000; // 0.77 x VCC (RESET)
    private static final byte VCOMH_DESELECT_LEVEL_083 = 0b0011_0000; // 0.83 x VCC
    private static final int BPP = 8;
    /**
     * ibid.
     */
    public static int DEFAULT_I2C_ADDRESS = 0x3C;
    public final int width;
    public final int height;
    public final int imageType;
    private final ShOledCommunicationChannel device;
    private final int pages;
    private final byte[] buffer;

    /**
     * Only known to come in two variations, based on height
     *
     * @param commChannel the comms
     * @param height      how tall
     */
    public SH1106(ShOledCommunicationChannel commChannel, Height height) {
        device = commChannel;
        width = WIDTH;
        this.height = height == Height.SHORT ? 32 : 64;
        imageType = BufferedImage.TYPE_BYTE_BINARY;
        buffer = new byte[this.width * this.height / BPP];
        pages = this.height / 8;

        init();
    }

    protected void init() {
        reset();

        setDisplayOn(false);

        byte multiplex = (byte) (height == 32 ? 0x20 : 0x3F);
        byte displayOffset = (byte) (height != 32 ? 0x00 : 0x0F); //0x0, // no offset

        command(
                //MEMORYMODE,
                (byte) 0x20,
                SET_HIGHER_COLUMN_START_ADDR, (byte) 0xB0, (byte) 0xC8,
                SET_LOWER_COLUMN_START_ADDR, (byte) 0x10, (byte) 0x40,
                SET_SEGMENT_REMAP_ON,
                NORMAL_DISPLAY,
                SET_MULTIPLEX_RATIO, multiplex,
                RESUME_TO_RAM_CONTENT_DISPLAY,
                SET_DISPLAY_OFFSET, displayOffset,
                DISPLAY_CLOCK_DIV_OSC_FREQ, (byte) 0xF0,
                SET_PRECHARGE_PERIOD, (byte) 0x22,
                SET_COM_PINS_HW_CONFIG, (byte) 0x12,
                SET_VCOMH_DESELECT_LEVEL, (byte) 0x20,
                SET_CHARGE_PUMP, (byte) 0x14,
                SET_CONTRAST, (byte) 0x7F);

        clear();

        setDisplayOn(true);
    }

    public void display(BufferedImage image) {
        display(image, 1);
    }

    public void display(BufferedImage image, int threshold) {
        //TODO convert to binary
        display(image, 0, 0);

    }


    public void display(BufferedImage image, int x, int y) {
        if (image.getWidth() != width || image.getHeight() != height) {
            throw new IllegalArgumentException("Invalid input image dimensions, must be " + width + "x" + height);
        }
/*        if (image.getType() != imageType) {
            throw new IllegalArgumentException("Image type mismatch excepted: " + imageType + " was: " + image.getType());
        }*/
        var tmpImage = new BufferedImage(width, height, imageType);
        tmpImage.getGraphics().drawImage(image, x, y, null);
        var index = 0;
        int pixelval;
        var pixels = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();
        for (int posY = 0; posY < height; posY++) {
            for (int posX = 0; posX < width / 8; posX++) {
                for (int bit = 0; bit < 8; bit++) {
                    pixelval = (pixels[ index / 8] >> 7 - bit & 0x01);
                    int x1 = posX * 8 + bit;
                    setPixel(x1, posY, pixelval > 0);
                    index++;
                }
            }
        }
        display();
    }


    public void  drawChar(char c, FontKt font, int x, int y, boolean on) {
        font.drawChar(this, c, x, y, on);
    }

    public void  drawString(String string, FontKt font, int x, int y, boolean on) {
        var posX = x;
        var posY = y;
        for (var c : string.toCharArray()) {
            if (c == '\n') {
                posY += font.outerHeight;
                posX = x;
            } else {
                if (posX >= 0 && posX + font.width < width && posY >= 0 && posY + font.height < height                ) {
                    drawChar(c, font, posX, posY, on);
                }
                posX += font.outerWidth;
            }
        }
    }


    public void  drawStringCentered(String string, FontKt font, int y, boolean on) {
        int strSizeX = string.length() * font.outerWidth;
        int x = (width - strSizeX) / 2;
        drawString(string, font, x, y, on);
    }


    public void  clearRect(int x, int y, int width, int height, boolean on) {
        for (int posX=x; posX<  x + width;posX++) {
            for (int posY=0;posY < y + height;posY++) {
                setPixel(posX, posY, on);
            }
        }
    }




    public void setPixel(int x, int y, boolean on) {
        int index = x + (y / BPP) * width;
        if (on) {
            buffer[index] |= (1 << (y & 7));
        } else {
            buffer[index] &= ~(1 << (y & 7));
        }
    }

    /**
     * Sets the display contract. Apparently not really working.
     *
     * @param contrast Contrast
     */
    public void setContrast(byte contrast) {
        command(SET_CONTRAST, contrast);
    }

    /**
     * Sets if the display should be inverted
     *
     * @param invert Invert state
     */
    public void invertDisplay(boolean invert) {
        command(invert ? INVERSE_DISPLAY : NORMAL_DISPLAY);
    }

    public int getImageType() {
        return imageType;
    }

    private void reset() {
        device.reset();
    }

    private void command(byte... commands) {
        device.sendCommand(commands);
    }

    private void data() {
        for (int i = 0; i < height / 8; i++) {
            command((byte) (SET_PAGE_START_ADDR + i), (byte) 0x02, (byte) 0x10);
            device.sendData(buffer, this.width * i, this.width);
        }
    }

    /**
     * Displays the current buffer contents.
     */
    public void display() {
        home();
        data();
    }

    private void home() {
        command(
                SET_LOWER_COLUMN_START_ADDR,
                SET_HIGHER_COLUMN_START_ADDR,
                SET_DISPLAY_START_LINE_0
        );
    }

    public void clear() {
        Arrays.fill(buffer, (byte) 0);
        display();
    }

    public void setDisplayOn(boolean on) {
        command(on ? DISPLAY_ON : DISPLAY_OFF);
    }

    @Override
    public void close() {
        log.trace("close()");
        clear();
        setDisplayOn(false);
        device.close();
    }


    /**
     * Scales the image to fit. This will scale up or down, depending on the relative sizes.
     * <p>
     * This <b>DOES NOT</b> display the image.
     * </p>
     *
     * @param image the image to scale
     * @return the scaled image
     */
    public BufferedImage scaleImage(BufferedImage image) {
        BufferedImage showThis = image;
        if (image.getWidth() != width || image.getHeight() != height) {
            float imageWd = image.getWidth();
            float imageHt = image.getHeight();
            float scale = Math.min(width / imageWd, height / imageHt);
            int w = (int) Math.floor(imageWd * scale);
            int y = (int) Math.floor(imageHt * scale);
            Image scaledInstance = image.getScaledInstance(w, y, Image.SCALE_DEFAULT);
            showThis = new BufferedImage(w, y, imageType);
            showThis.getGraphics().drawImage(scaledInstance, 0, 0, null);
        }
        return showThis;
    }

    /**
     * Creates a default font (SERIF, PLAIN) that will fit in the specified number of "lines" with 0 spacing.
     *
     * @param numberOfLines number of lines
     * @return the font
     */
    public Font defaultFont(int numberOfLines) {
        return fitLines(Font.SANS_SERIF, Font.PLAIN, numberOfLines, 0);
    }

    /**
     * Creates a font of the specified type that will fit on the full display with the specified number of lines,
     * with the number of <b>pixels</b> between each line.
     *
     * @param fontName      name of the font
     * @param fontType      font type
     * @param numberOfLines number of lines
     * @param lineSpacing   number of pixels between lines
     * @return the font that can be used to match these parameters
     */
    public Font fitLines(String fontName, int fontType, int numberOfLines, int lineSpacing) {
        // how big is the font in pixels
        int pixelsPerLine = height / numberOfLines - lineSpacing;
        int size = pixelsPerLine + 1;
        int fontSize = 48;    // TODO this is pretty big, but is it big enough?
        Font f = null;
        BufferedImage bufferedImage = new BufferedImage(width, height, imageType);
        Graphics2D g = bufferedImage.createGraphics();
        while (size > pixelsPerLine) {
            fontSize--;
            if (fontSize <= 0) throw new IllegalStateException("Font size is 0!");
            f = new Font(fontName, fontType, fontSize);
            size = g.getFontMetrics(f).getHeight();
        }
        return f;
    }

    public enum Height {
        SHORT, TALL
    }
}
