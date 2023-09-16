package com.dfi.sbc2ha.services.stats.provider;

import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.dfi.sbc2ha.services.state.StateManager;
import com.dfi.sbc2ha.services.state.StateManagerListener;
import lombok.Data;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OutputDataProvider extends DataProvider implements StateManagerListener {
    private static final char ON = '0';
    private static final char OFF = ' ';
    private final StateManager manger;
    private final List<String> keys;
    private int page = -1;
    private int outputsPerPage = -1;

    public OutputDataProvider(StateManager manger, List<String> keys) {
        super(null, -1, -1, TimeUnit.SECONDS);

        this.manger = manger;
        this.keys = keys;
    }

    public static List<Set<String>> split(List<String> originalSet, int count) {
        int length = (int) Math.ceil((double) originalSet.size() / count);
        List<Set<String>> result = new ArrayList<>(count);
        for (int i = 0; i < length; i++) {
            result.add(new LinkedHashSet<>());
        }
        int index = 0;
        for (String object : originalSet) {
            Set<String> rs = result.get(index++ / count);

            rs.add(object);
        }

        return result;
    }

    public boolean hasNextPage() {
        boolean b = keys.size() / outputsPerPage > page;
        if (!b) {
            page = -1;
        }
        return b;
    }

    public List<?> getOutputStates() {

        List<KeyValue> out = new LinkedList<>();
        Set<String> currentPageKeys = getCurrentPageKeys(keys);

        for (String k : currentPageKeys) {
            ScalarEvent stateEvent = (ScalarEvent) manger.get(k);
            boolean value = stateEvent== null ? false : stateEvent.getValue() > 0;
            out.add(new KeyValue(k, value ? String.valueOf(ON) : String.valueOf(OFF)));
        }

        return out;
    }

    private Set<String> getCurrentPageKeys(List<String> keys) {
        if (outputsPerPage < 0 || page < 0) {
            return new LinkedHashSet<>(keys);
        }
        List<Set<String>> split = split(keys, outputsPerPage);
        return split.get(page);
    }

    @Override
    public void schedule() {
        manger.addListener(this);
    }

    @Override
    public void stop() {
        manger.removeListener(this);
    }

    @Override
    public void addListener(Consumer<List<?>> consumer) {
        super.addListener(consumer);
    }

    @Override
    protected void onChange() {
        List<?> outputStates = getLines();
        synchronized (listeners) {
            listeners.forEach(c -> c.accept(outputStates));
        }
    }


    @Override
    public List<?> getLines() {
        return getOutputStates();
    }


    @Override
    public void run() {

    }

    @Override
    public void onStateChange() {
        onChange();
    }

    public void nextPage() {
        if (page < 0) {
            page = 0;
            return;
        }
        page++;
    }

    public void setOutputsPerPage(int outputsPerPage) {
        if (this.outputsPerPage < 0) {
            this.outputsPerPage = outputsPerPage;
            page = 0;
        } else if (this.outputsPerPage != outputsPerPage) {
            this.outputsPerPage = outputsPerPage;
            page = 0;
        }
    }

    @Data
    public static class KeyValue {
        final String key;
        final String value;
    }
}
