package com.dfi.sbc2ha.helper.extensionBoard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionModes {
    Map<String, Map<Integer, List<String>>> def = new HashMap<>();

    private ExtensionModes() {
    }

    public static ExtensionModes ofBoneIo(List<String[]> list) {
        ExtensionModes extensionModes = new ExtensionModes();
        for (var def : list) {
            String header = def[0];
            int pin = Integer.parseInt(def[1]);
            String[] modes = new String[def.length - 2];
            System.arraycopy(def, 2, modes, 0, def.length - 2);


            extensionModes.add(header, pin, modes);

        }
        return extensionModes;
    }

    private void add(String header, int pin, String[] modes) {
        if (!def.containsKey(header)) {
            def.put(header, new HashMap<>());
        }
        Map<Integer, List<String>> headerMap = def.get(header);
        headerMap.put(pin, List.of(modes));
    }

    public boolean hasPullUp(String header, int pin) {
        return def.get(header).get(pin).contains("gpio_pu");
    }
}
