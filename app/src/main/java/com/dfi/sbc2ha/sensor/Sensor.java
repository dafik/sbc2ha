package com.dfi.sbc2ha.sensor;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.state.State;
import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Consumer;
@Slf4j
public abstract class Sensor implements DeviceInterface {
    protected final String name;
    protected final Map<State, Collection<Consumer<StateEvent>>> listeners = new HashMap<>();
    protected final Collection<Consumer<StateEvent>> listenersAny = new LinkedHashSet<>();
    private boolean delegateListenerEnabled;

    public Sensor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract protected void setDelegateListener();

    abstract protected void removeDelegateListener();

    abstract protected void closeDelegate();


    /**
     * Add a new listener
     *
     * @param listener Callback instance
     */
    public void addListener(Consumer<StateEvent> listener, State event) {
        if (listenersEmpty()) {
            enableDelegateListener();
        }
        if (!listeners.get(event).contains(listener)) {
            listeners.get(event).add(listener);
        }
    }

    /**
     * Remove a specific listener
     *
     * @param listener Callback instance to remove
     */
    public void removeListener(Consumer<StateEvent> listener, State event) {
        listeners.get(event).remove(listener);
        if (listenersEmpty()) {
            disableDelegateListener();
        }
    }

    public void addListenerAny(Consumer<StateEvent> listener) {
        if (listenersEmpty()) {
            enableDelegateListener();
        }
        if (!listenersAny.contains(listener)) {
            listenersAny.add(listener);
        }
    }

    /**
     * Remove a specific listener
     *
     * @param listener Callback instance to remove
     */
    public void removeListenerAny(Consumer<StateEvent> listener) {
        listenersAny.remove(listener);
        if (listenersEmpty()) {
            disableDelegateListener();
        }
    }

    public boolean listenersEmpty() {
        int sum = listeners.values().stream()
                .mapToInt(Collection::size).sum();
        return sum + listenersAny.size() <= 0;
    }

    /**
     * Remove all listeners
     */
    public void removeAllListeners() {
        listeners.clear();
        listenersAny.clear();
        disableDelegateListener();
    }

    protected void enableDelegateListener() {
        if (!delegateListenerEnabled) {
            setDelegateListener();
            delegateListenerEnabled = true;
        }
    }

    private void disableDelegateListener() {
        // Ignore if there is an activated / deactivated consumer or listeners
        if (listenersEmpty()) {
            if (delegateListenerEnabled) {
                removeDelegateListener();
                delegateListenerEnabled = false;
            }
        }
    }

    @Override
    public void close() throws RuntimeIOException {
        log.trace("close()");
        closeDelegate();
        removeAllListeners();
    }

    protected void handleAny(StateEvent event) {
        listenersAny.forEach(listener -> listener.accept(event));
    }

    public void whenAny(Consumer<StateEvent> consumer) {
        addListenerAny(consumer);
    }

    public enum Direction {
        PRESS,
        RELEASE
    }

    public abstract static class Builder<T extends Builder<T>> {
        protected String name;

        protected abstract T self();


        public T setName(String name) {

            this.name = name;
            return self();
        }

    }
}
