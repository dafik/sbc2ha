package com.dfi.sbc2ha.helper.extensionBoard;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionOutputBoard {
    private final Map<Integer, OutputDefinition> def = new HashMap<>();

    public static ExtensionOutputBoard ofBoneIo(List<String[]> entries) {
        ExtensionOutputBoard extensionOutputBoard = new ExtensionOutputBoard();

        List<Integer> busAddress = List.of(
                Integer.decode(entries.remove(0)[0]),
                Integer.decode(entries.remove(0)[0])
        );


        for (var entry : entries) {
            int busId = Integer.parseInt(entry[1]);
            int pin = Integer.parseInt(entry[2]);
            String type = entry[3].toUpperCase();
            OutputDefinition od = new OutputDefinition(type, busId, pin);
            od.setAddress(busAddress.get(busId - 1));

            extensionOutputBoard.add(Integer.parseInt(entry[0]), od);
        }
        return extensionOutputBoard;
    }

    public static ExtensionOutputBoard ofRpi(List<String[]> entries) {
        ExtensionOutputBoard extensionOutputBoard = new ExtensionOutputBoard();

        for (var entry : entries) {
            int gpio = Integer.parseInt(entry[2]);
            String type = entry[3].toUpperCase();
            OutputDefinition od = new OutputDefinition(type, -1, gpio);


            extensionOutputBoard.add(Integer.parseInt(entry[0]), od);
        }
        return extensionOutputBoard;
    }
    public static ExtensionOutputBoard of(List<String[]> entries) {
        ExtensionOutputBoard extensionOutputBoard = new ExtensionOutputBoard();

        for (var entry : entries) {
            int gpio = Integer.parseInt(entry[2]);
            String type = entry[3].toUpperCase();
            OutputDefinition od = new OutputDefinition(type, -1, gpio);


            extensionOutputBoard.add(Integer.parseInt(entry[0]), od);
        }
        return extensionOutputBoard;
    }


    public OutputDefinition get(int outputNumber) {
        return def.get(outputNumber);
    }

    private void add(int output, OutputDefinition outputDefinition) {
        def.put(output, outputDefinition);
    }

    public int getIdMcp(int pin, int mcpId) {
        int found = -1;
        for (Map.Entry<Integer, OutputDefinition> entry : def.entrySet()) {
            Integer integer = entry.getKey();
            OutputDefinition od = entry.getValue();

            if (od.getPin() == pin && od.getId() == mcpId && od.getType().equals("MCP")) {
                found = integer;
                break;
            }
        }

        return found;
    }
    public int getByGpioId(int gpio) {
        int found = -1;
        for (Map.Entry<Integer, OutputDefinition> entry : def.entrySet()) {
            Integer integer = entry.getKey();
            OutputDefinition od = entry.getValue();

            if (od.getPin() == gpio && od.getType().equals("GPIO")) {
                found = integer;
                break;
            }
        }

        return found;
    }

    @Data
    public static class OutputDefinition {
        final String type;
        final int id;
        final int pin;
        int address;
    }
}
