package com.diozero.devices.oled;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FontKt {
    final int width;
    final int height;
    final int outerWidth;
    final int outerHeight;
    private final int minChar;
    private final int maxChar;
    private final byte[] data;

    public FontKt(int minChar, int maxChar, int width, int height, int outerWidth, int outerHeight, byte[] data) {
        this.minChar = minChar;
        this.maxChar = maxChar;
        this.width = width;
        this.height = height;
        this.outerWidth = outerWidth;
        this.outerHeight = outerHeight;
        this.data = data;
    }


    public static FontKt FONT_5X8() {
        return new FontKt(0, 255, 5, 8, 6, 9, readFromFile("FONT_5X8"));
    }

    public static FontKt FONT_4X5() {
        return new FontKt(32, 95, 4, 5, 4, 7, readFromFile("FONT_4X5"));

    }

    public static byte[] readFromFile(String filename) {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(filename);
        assert inputStream != null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<>();
        bufferedReader.lines().forEach(lines::add);
        List<Byte> bytes = new ArrayList<>();
        for (String line : lines) {
            bytes.add((byte) Integer.parseInt(line.substring(2), 16));
        }
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }
        return byteArray;
    }

    public byte getData(int offset) {
        return data[offset];
    }

    void drawChar(SH1106 display, char _c, int x, int y, boolean on) {
        char c = _c;
        if ((int) c > maxChar || (int) c < minChar) {
            c = '?';
        }
        c -= minChar;
        for (int i = 0; i < width; i++) {
            int line = (int) data[((int) c * width) + i];
            for (int j = 0; j < height; j++) {
                if ((line & 0x01) > 0) {
                    display.setPixel(x + i, y + j, on);
                }
                line = line >> 1;
            }
        }
    }
}
