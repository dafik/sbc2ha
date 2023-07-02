package com.dfi.sbc2ha.helper.stats.provider;

import com.dfi.sbc2ha.helper.StateManager;
import com.dfi.sbc2ha.helper.StateManagerListener;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OutputDataProvider extends DataProvider<Map<String, String>> implements StateManagerListener {
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

    public Map<String, String> getOutputStates() {

        Map<String, String> out = new LinkedHashMap<>();
        Set<String> currentPageKeys = getCurrentPageKeys(keys);

        for (String k : currentPageKeys) {
            boolean value = (int) manger.get(SbcDeviceType.RELAY.toString(), k) > 0;
            out.put(k, value ? String.valueOf(ON) : String.valueOf(OFF));
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
    public DataProvider<Map<String, String>> schedule() {
        manger.addListener(this);
        return this;
    }

    @Override
    public DataProvider<Map<String, String>> stop() {
        manger.removeListener(this);
        return this;
    }

    @Override
    public void addListener(Consumer<Map<String, String>> consumer) {
        super.addListener(consumer);
    }

    @Override
    protected void onChange() {
        Map<String, String> outputStates = getLines();
        synchronized (listeners) {
            listeners.forEach(c -> c.accept(outputStates));
        }
    }


    @Override
    public Map<String, String> getLines() {
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
}
