package com.dfi.sbc2ha.helper;

import com.dfi.sbc2ha.actuator.Actuator;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActuatorType;
import com.dfi.sbc2ha.event.StateEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StateManager {
    private static final String FILE_NAME = "state.json";
    private static StateManager instance;
    private final ObjectMapper mapper;
    private final List<StateManagerListener> listeners = new ArrayList<>();
    private final ObjectWriter objectWriter;
    private Map<ActuatorType, States> states = new HashMap<>();

    private StateManager() {
        mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
        //objectWriter = mapper.writer(filterProvider).withDefaultPrettyPrinter();
        objectWriter = mapper.writer(filterProvider).withDefaultPrettyPrinter();
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
        TypeReference<Map<ActuatorType, States>> typeRef = new TypeReference<>() {
        };

        try {
            states = mapper.readValue(new FileInputStream(statsFile), typeRef);
        } catch (IOException e) {
            log.error("unable load last state, skip reason: {}", e.getMessage());
            states = new HashMap<>();
        }
    }

    private synchronized void saveState() throws IOException {
        File statsFile = new File(getFileLocation()).getAbsoluteFile();
        try {
            objectWriter.writeValue(statsFile, states);
            String s = objectWriter.writeValueAsString(states);
            var x = 1;
        } catch (RuntimeException e) {
            log.error("write error", e);
        }

    }

    public void save(ActuatorType type, String attr, StateEvent value) {
        if (!states.containsKey(type)) {
            states.put(type, new States());
        }
        states.get(type).put(attr, value);
        onChange();

        try {
            saveState();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StateEvent get(ActuatorType type, String attr) {
        if (!states.containsKey(type) || !states.get(type).containsKey(attr)) {
            return null;
        }
        return states.get(type).get(attr);
    }

    public StateEvent get(String attr) {
        for (States entries : states.values()) {
            if (entries.containsKey(attr)) {
                return entries.get(attr);
            }
        }
        return null;
    }

    public void remove(ActuatorType type, String attr) {
        if (states.containsKey(type)) {
            return;
        }
        Map<String, StateEvent> typeStates = states.get(type);
        typeStates.remove(attr);
        if (typeStates.isEmpty()) {
            states.remove(type);
        }
    }


    public void handlerState(ActuatorType outputType, Actuator actuator, StateEvent actuatorEvent) {
        save(outputType, actuator.getName(), actuatorEvent);

    }

    public static class States extends HashMap<String, StateEvent> {

    }
}
