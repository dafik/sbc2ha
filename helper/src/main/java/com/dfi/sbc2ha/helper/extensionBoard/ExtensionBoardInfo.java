package com.dfi.sbc2ha.helper.extensionBoard;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtensionBoardInfo {
    private static ExtensionBoardInfo instance;
    private final ExtensionInputBoard in;
    private final ExtensionOutputBoard out;
    private static final String PIN_SEPARATOR = "_";


    public int getInputByPin(String type,String pin) {

        if (!pin.contains(PIN_SEPARATOR)) {
            throw new IllegalArgumentException("pin not contain '_' pin: " + pin);
        }
        String[] parts = pin.split(PIN_SEPARATOR);
        int id = getIn().getId(type,parts[0], Integer.parseInt(parts[1]));
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

    private ExtensionBoardInfo(ExtensionInputBoard in, ExtensionOutputBoard out) {
        this.in = in;
        this.out = out;
    }


    public static void initialize(String vendor, String inputBoard, String outputBoard) {
        if (Objects.equals(vendor, "boneio")) {
            ExtensionModes modes = ExtensionModes.of(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + "bbb-modes.txt"));
            ExtensionInputBoard in = ExtensionInputBoard.of(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + inputBoard + ".txt"), modes);
            ExtensionOutputBoard out = ExtensionOutputBoard.of(loadBoardPinInfoDefinition("extensionBoardDef/" + vendor + "/" + outputBoard + ".txt"));

            instance = new ExtensionBoardInfo(in, out);
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
        Logger.trace("Looking for extension board def file '{}'", boardDefFile);
        try (InputStream is = ExtensionBoardInfo.class.getClassLoader().getResourceAsStream(boardDefFile)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    Logger.debug("Reading board defs file {}", boardDefFile);
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

    public ExtensionInputBoard getIn() {
        return in;
    }

    public ExtensionOutputBoard getOut() {
        return out;
    }
}
