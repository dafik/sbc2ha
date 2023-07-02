package com.dfi.sbc2ha.helper;

import com.dfi.sbc2ha.actuator.*;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateManager {
    private static final String FILE_NAME = "state.json";
    private static StateManager instance;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<StateManagerListener> listeners = new ArrayList<>();
    private Map<String, Map<String, Number>> states = new HashMap<>();

    private StateManager() {
        loadState();
    }

    public static StateManager getInstance() {
        if (null == instance) {
            instance = new StateManager();
        }
        return instance;
    }

    private static String getFileLocation() {
        return JarHelper.getTempDir() + "/" + FILE_NAME;
    }


    public void addListener(StateManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StateManagerListener listener) {
        while (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void clearListeners() {
        listeners.clear();
    }

    private void onChange() {
        for (var listener : listeners) {
            listener.onStateChange();
        }
    }

    private void loadState() {
        File file = new File(getFileLocation());
        File statsFile = file.getAbsoluteFile();
        if (!statsFile.exists()) {
            return;
        }
        TypeReference<Map<String, Map<String, Number>>> typeRef = new TypeReference<>() {
        };

        try {
            states = mapper.readValue(new FileInputStream(statsFile), typeRef);
        } catch (IOException e) {
            Logger.error("unable load last state, skip reason: {}", e.getMessage());
            states = new HashMap<>();
        }
    }

    private synchronized void saveState() throws IOException {
        File statsFile = new File(getFileLocation()).getAbsoluteFile();
        mapper.writeValue(statsFile, states);
    }

    public void save(String type, String attr, Number value) {
        if (!states.containsKey(type)) {
            states.put(type, new HashMap<>());
        }
        states.get(type).put(attr, value);
        onChange();

        try {
            saveState();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Number get(String type, String attr) {
        if (!states.containsKey(type) || !states.get(type).containsKey(attr)) {
            return 0;
        }
        return states.get(type).get(attr);
    }

    public void remove(String type, String attr) {
        if (states.containsKey(type)) {
            return;
        }
        Map<String, Number> typeStates = states.get(type);
        typeStates.remove(attr);
        if (typeStates.isEmpty()) {
            states.remove(type);
        }
    }

    public void handlerState(Actuator<?, ?, ?> actuator, RelayEvent actuatorEvent) {
        if (actuator instanceof Relay) {
            save(SbcDeviceType.RELAY.toString(), actuator.getName(), actuatorEvent.toInt());
        }
    }

    public void handlerState(Actuator<?, ?, ?> actuator, LedEvent actuatorEvent) {
        if (actuator instanceof Led) {
            save(SbcDeviceType.RELAY.toString(), actuator.getName(), actuatorEvent.getBrightnessRaw());
        }
    }
}
