package com.dfi.sbc2ha.helper.extensionBoard;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public class ExtensionBoardInfo {
    private static final String PIN_SEPARATOR = "_";
    private static ExtensionBoardInfo instance;
    private final String vendor;
    private final ExtensionInputBoard in;
    private final ExtensionOutputBoard out;

    private final boolean fake;


    private ExtensionBoardInfo(String vendor, ExtensionInputBoard in, ExtensionOutputBoard out) {
        this.vendor = vendor;
        this.in = in;
        this.out = out;

        this.fake = vendor.equals("fake");
        if(this.fake) {
            System.getProperties().setProperty("diozero.devicefactory", "com.diozero.devices.fake.FakeDeviceFactory");
        }


    }

    public static void initialize(String vendor, String inputBoard, String outputBoard) {
        if (Objects.equals(vendor, "boneio")) {
            ExtensionModes modes = ExtensionModes.ofBoneIo(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + "bbb-modes.txt"));
            ExtensionInputBoard in = ExtensionInputBoard.ofBoneIo(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + inputBoard + ".txt"), modes);
            ExtensionOutputBoard out = ExtensionOutputBoard.ofBoneIo(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + outputBoard + ".txt"));

            instance = new ExtensionBoardInfo(vendor, in, out);
        } else if(Objects.equals(vendor, "rpi")){
            ExtensionInputBoard in = ExtensionInputBoard.ofRpi(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + inputBoard + ".txt"));
            ExtensionOutputBoard out = ExtensionOutputBoard.ofRpi(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + outputBoard + ".txt"));

            instance = new ExtensionBoardInfo(vendor, in, out);
        } else {
            ExtensionInputBoard in = ExtensionInputBoard.of(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + inputBoard + ".txt"));
            ExtensionOutputBoard out = ExtensionOutputBoard.of(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + outputBoard + ".txt"));

            instance = new ExtensionBoardInfo(vendor, in, out);
        }
    }

    public static ExtensionBoardInfo getInstance() {
        if (instance == null) {
            throw new RuntimeException("not initialized");
        }
        return instance;
    }

    protected static List<String[]> loadBoardPinInfoDefinition(String boardDefFile) {
        List<String[]> out = new ArrayList<>();
        log.trace("Looking for extension board def file '{}'", boardDefFile);
        try (InputStream is = ExtensionBoardInfo.class.getClassLoader().getResourceAsStream(boardDefFile)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    log.debug("Reading board defs file {}", boardDefFile);
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();

                        // Strip comments
                        int index = line.indexOf("#");
                        if (index != -1) {
                            line = line.substring(0, index);
                        }
                        // Ignore empty lines
                        if (line.isEmpty()) {
                            continue;
                        }
                        String[] parts = line.split(",");
                        out.add(parts);
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        }

        return out;
    }

    public int getInputByPin(String type, String pin) {

        if (!pin.contains(PIN_SEPARATOR)) {
            throw new IllegalArgumentException("pin not contain '_' pin: " + pin);
        }
        String[] parts = pin.split(PIN_SEPARATOR);
        int id = getIn().getId(type, parts[0], Integer.parseInt(parts[1]));
        if (id == -1) {
            throw new IllegalArgumentException("input not found: " + pin);
        }
        return id;

    }

    public int getOutputMcpByPin(int pin, int mcpId) {
        int id = getOut().getIdMcp(pin, mcpId);
        if (id == -1) {
            throw new IllegalArgumentException("output not found: mcp" + mcpId + " pin: " + pin);
        }
        return id;
    }

    public int getOutputRpiByGpio(int gpio) {
        int id = getOut().getByGpioId(gpio);
        if (id == -1) {
            throw new IllegalArgumentException("output not found: gpio: " + gpio);
        }
        return id;
    }


}
