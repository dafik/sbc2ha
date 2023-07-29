package com.dfi.sbc2ha.platform.oled;

import com.dfi.sbc2ha.Version;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ScreenType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.OledConfig;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.helper.stats.StatsProvider;
import com.dfi.sbc2ha.helper.stats.provider.*;
import com.dfi.sbc2ha.helper.stats.provider.OutputDataProvider.KeyValue;
import com.dfi.sbc2ha.helper.timer.ClickTimerTask;
import com.dfi.sbc2ha.sensor.binary.Button;
import com.dfi.sbc2ha.util.DataUtil;
import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.oled.SH1106;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Display implements DeviceInterface {
    private final SH1106 displayDelegate;
    private final Button inputDelegate;
    private final StatsProvider hostData;
    private final int screensaverTimeout;
    private final List<ScreenType> screens;
    private final Font fontBig;
    private final Font fontSmall;
    private final Font fontExtraSmall;
    private final Font danube;
    private final List<String> bootLines = new ArrayList<>();
    private ClickTimerTask screensaverTask;
    private ScreenType currentScreen;
    private boolean started = false;

    public Display(SH1106 displayDevice, Button inputDelegate, OledConfig config, StatsProvider hostData) {
        this.displayDelegate = displayDevice;
        this.inputDelegate = inputDelegate;
        this.hostData = hostData;
        long seconds = config.getScreensaverTimeout().toSeconds();
        if (seconds > Integer.MAX_VALUE) {
            seconds = Integer.MAX_VALUE;
        }
        screensaverTimeout = Long.valueOf(seconds).intValue();
        screens = config.getScreens().size() > 0 ? config.getScreens() : OledConfig.DEFAULT_SCREENS;
        if (screens.size() == 0) {
            screens.add(ScreenType.UPTIME);
        }

/*
        fontBig = make_font("DejaVuSans.ttf", 12)
        fontSmall = make_font("DejaVuSans.ttf", 9)
        fontExtraSmall = make_font("DejaVuSans.ttf", 7)
        danube = make_font("danube__.ttf", 15, local=True)
*/
        String dejavu = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf";
        String dejavuMono = "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf";

        InputStream systemResource = ClassLoader.getSystemResourceAsStream("danube__.ttf");


        try {
            fontBig = Font.createFont(Font.TRUETYPE_FONT, new File(dejavu)).deriveFont(12f);
            fontSmall = Font.createFont(Font.TRUETYPE_FONT, new File(dejavu)).deriveFont(9f);
            fontExtraSmall = Font.createFont(Font.TRUETYPE_FONT, new File(dejavuMono)).deriveFont(8f);
            assert systemResource != null;
            danube = Font.createFont(Font.TRUETYPE_FONT, systemResource).deriveFont(15f);

        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }


    }


    private static Graphics2D getCanvas(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.white);
        g2d.setBackground(Color.black);
        return g2d;
    }

    @SuppressWarnings("SameParameterValue")
    private static String subOrPad(String val, int length, boolean right) {
        if (right) {
            return (val + " ".repeat(length)).substring(0, length);
        }
        String s = (" ".repeat(length)) + val;
        return s.substring(s.length() - length);
    }

    public void handleClick(StateEvent evt) {
        if (!started) {
            drawBoot();
        }
        stopSleepHandler();
        OutputDataProvider dp = (OutputDataProvider) hostData.getProvider(ScreenType.OUTPUTS);
        if (currentScreen == null) {
            currentScreen = screens.get(0);
        } else {
            if (currentScreen != ScreenType.OUTPUTS || !dp.hasNextPage()) {
                int next = screens.indexOf(currentScreen) + 1;
                if (next > screens.size() - 1) {
                    next = 0;
                }
                currentScreen = screens.get(next);
            }
        }
        settleSleepHandler();
        try {
            renderScreen();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    private void renderScreen() {
        List<String> lines;
        hostData.clearListeners();
        switch (currentScreen) {
            case UPTIME:
                hostData.addListener(currentScreen, this::drawUptime);
                lines = ((UptimeDataProvider) hostData.getProvider(currentScreen)).getLines();
                drawUptime(lines);
                break;
            case NETWORK:
                hostData.addListener(currentScreen, this::drawNetwork);
                lines = ((NetworkDataProvider) hostData.getProvider(currentScreen)).getLines();
                drawNetwork(lines);
                break;
            case CPU:
                hostData.addListener(currentScreen, this::drawCpu);
                lines = ((CpuDataProvider) hostData.getProvider(currentScreen)).getLines();
                drawCpu(lines);
                break;
            case DISK:
                hostData.addListener(currentScreen, this::drawDisk);
                lines = ((DiskDataProvider) hostData.getProvider(currentScreen)).getLines();
                drawDisk(lines);
                break;
            case MEMORY:
                hostData.addListener(currentScreen, this::drawMemory);
                lines = ((MemoryDataProvider) hostData.getProvider(currentScreen)).getLines();
                drawMemory(lines);
                break;
            case SWAP:
                hostData.addListener(currentScreen, this::drawSwap);
                lines = ((SwapDataProvider) hostData.getProvider(currentScreen)).getLines();
                drawSwap(lines);
                break;
            case OUTPUTS:
                hostData.addListener(currentScreen, this::drawOutputs);
                OutputDataProvider outputDataProvider = (OutputDataProvider) hostData.getProvider(currentScreen);
                outputDataProvider.nextPage();
                outputDataProvider.setOutputsPerPage(12);
                List<?> outputStates = outputDataProvider.getLines();
                drawOutputs(outputStates);
                break;
        }
        displayDelegate.setDisplayOn(true);
    }

    private void drawStandard(List<?> list) {

        BufferedImage image = getNewImage();
        Graphics2D g2d = getCanvas(image);

        g2d.setFont(fontBig);
        int line = 15;
        int step = 10;
        g2d.drawString((String) list.get(0), 3, line);
        g2d.setFont(fontSmall);

        for (var i = 1; i < list.size(); i++) {
            line += step;
            g2d.drawString((String) list.get(i), 3, line);
        }
        displayDelegate.display(image);
    }

    private void drawOutputs(List<?> outputStates) {
        log.trace("display outputs");

        List<KeyValue> collect = outputStates.stream().map(o -> (KeyValue) o).collect(Collectors.toList());

        BufferedImage image = getNewImage();
        Graphics2D g2d = getCanvas(image);

        if (outputStates.isEmpty()) {
            g2d.setFont(fontSmall);
            g2d.drawString("no output present: ", 3, 10);
            displayDelegate.display(image);
            return;
        }

        try {
            g2d.setFont(fontBig);
            int line = 0;
            //g2d.drawString("outputs", 3, line);

            g2d.setFont(fontExtraSmall);

            List<List<KeyValue>> lists = DataUtil.split(collect, 6);

            for (KeyValue entry : lists.get(0)) {
                String i = entry.getKey();
                String s = entry.getValue();
                line += 10;
                g2d.drawString(subOrPad(i, 10, true) + " " + s, 3, line);
            }
            if (lists.size() > 1) {
                line = 0;
                for (KeyValue entry : lists.get(1)) {
                    String i = entry.getKey();
                    String s = entry.getValue();
                    line += 10;
                    g2d.drawString(subOrPad(i, 10, true) + " " + s, 65, line);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        displayDelegate.display(image);
    }

    private void drawSwap(List<?> lines) {
        log.trace("display swap");
        drawStandard(lines);
    }

    private void drawMemory(List<?> lines) {
        log.trace("display memory");
        drawStandard(lines);
    }

    private void drawDisk(List<?> lines) {
        log.trace("display disk");
        drawStandard(lines);
    }

    private void drawCpu(List<?> lines) {
        log.trace("display cpu:");
        drawStandard(lines);
    }

    private void drawNetwork(List<?> lines) {
        log.trace("display network");
        drawStandard(lines);
    }

    private void drawUptime(List<?> lines) {
        log.trace("display uptime");

        BufferedImage image = getNewImage();
        Graphics2D g2d = getCanvas(image);

        g2d.setFont(danube);

        g2d.drawString("bone", 3, 15);
        g2d.drawString("iO", 53, 15);
        g2d.drawString("java", 73, 15);

        g2d.setFont(fontSmall);
        g2d.drawString("version: " + Version.VERSION, 3, 30);

        try {
            g2d.drawString((String) lines.get(0), 3, 42);
            g2d.drawString((String) lines.get(1), 3, 54);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        displayDelegate.display(image);
    }

    public void addBootLine(String line) {
        bootLines.add(line);
        drawBoot();
        displayDelegate.setDisplayOn(true);
    }

    public void setStarted() {
        started = true;
    }

    private void drawBoot() {
        log.trace("display boot");

        BufferedImage image = getNewImage();
        Graphics2D g2d = getCanvas(image);

        g2d.setFont(danube);

        int offset = 15;
        g2d.drawString("bone", 3, offset);
        g2d.drawString("iO", 53, offset);
        g2d.drawString("java", 73, offset);
        offset += 2;
        g2d.setFont(fontExtraSmall);
        try {
            List<String> list = bootLines.subList(Math.max(bootLines.size() - 5, 0), bootLines.size());
            for (String l : list) {
                offset += 8;
                g2d.drawString(l, 3, offset);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        displayDelegate.display(image);
    }

    private BufferedImage getNewImage() {
        int width = displayDelegate.width;
        int height = displayDelegate.height;

        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private void sleep(long time) {
        hostData.clearListeners();
        displayDelegate.clear();
        displayDelegate.setDisplayOn(false);
        currentScreen = null;
    }

    private void stopSleepHandler() {
        if (screensaverTask != null) {
            screensaverTask.cancel();
        }
    }

    private void settleSleepHandler() {
        if (screensaverTask != null) {
            screensaverTask.cancel();
        }
        screensaverTask = new ClickTimerTask(this::sleep, screensaverTimeout * 1000);
        screensaverTask.schedule();
    }

    @Override
    public void close() throws RuntimeIOException {
        inputDelegate.close();
        displayDelegate.close();
    }
}
