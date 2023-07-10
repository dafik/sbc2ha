package com.dfi.sbc2ha.helper.extensionBoard;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionInputBoard {
    private final Map<Integer, InputDefinition> digital = new HashMap<>();
    private final Map<Integer, InputDefinition> analog = new HashMap<>();
    private final Map<Integer, InputDefinition> casing = new HashMap<>();

    public static ExtensionInputBoard ofBoneIo(List<String[]> entries, ExtensionModes modes) {
        ExtensionInputBoard extensionInputBoard = new ExtensionInputBoard();

        for (var entry : entries) {
            int pin = Integer.parseInt(entry[3]);
            InputDefinition inputDefinition = new InputDefinition(ExtensionInputType.valueOfChar(entry[0]), entry[2], pin);
            if (entry[0].equals("D")) {
                boolean pullUp = modes.hasPullUp(entry[2], pin);
                inputDefinition.setPullUp(pullUp);
            }
            extensionInputBoard.add(entry[0], Integer.parseInt(entry[1]), inputDefinition);
        }
        return extensionInputBoard;
    }

    public static ExtensionInputBoard ofRpi(List<String[]> entries) {
        ExtensionInputBoard extensionInputBoard = new ExtensionInputBoard();

        for (var entry : entries) {
            int pin = Integer.parseInt(entry[0]);
            int gpio = Integer.parseInt(entry[2]);

            InputDefinition inputDefinition = new InputDefinition(ExtensionInputType.DIGITAL, null, gpio);
            extensionInputBoard.add("D", pin, inputDefinition);
        }
        return extensionInputBoard;
    }

    private void add(String type, int inputNumber, InputDefinition inputDefinition) {
        switch (type) {
            case "A":
                analog.put(inputNumber, inputDefinition);
                return;
            case "D":
                digital.put(inputNumber, inputDefinition);
                return;
            case "C":
                casing.put(inputNumber, inputDefinition);
                return;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    public int getId(String type, String header, int pin) {
        switch (type) {
            case "A":
                return getId(analog, header, pin);
            case "D":
                return getId(digital, header, pin);
            case "C":
                return getId(casing, header, pin);
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    public Map<Integer, InputDefinition> get(String type) {
        switch (type) {
            case "A":
                return analog;
            case "D":
                return digital;
            case "C":
                return casing;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    private int getId(Map<Integer, InputDefinition> def, String header, int pin) {
        int found = -1;
        for (Map.Entry<Integer, InputDefinition> entry : def.entrySet()) {
            Integer integer = entry.getKey();
            InputDefinition inputDefinition = entry.getValue();
            if (inputDefinition.getHeader().equals(header) && inputDefinition.getPin() == pin) {
                found = integer;
                break;
            }
        }
        return found;
    }

    public enum ExtensionInputType {
        ANALOG,
        DIGITAL,
        CASE;

        public static ExtensionInputType valueOfChar(String c) {
            switch (c) {
                case "A":
                    return ExtensionInputType.ANALOG;
                case "D":
                    return ExtensionInputType.DIGITAL;
                case "C":
                    return ExtensionInputType.CASE;
                default:
                    throw new IllegalArgumentException("unknown type: " + c);
            }
        }
    }

    @Data
    public static class InputDefinition {
        final ExtensionInputType type;
        final String header;
        final int pin;
        boolean pullUp;
    }

}
