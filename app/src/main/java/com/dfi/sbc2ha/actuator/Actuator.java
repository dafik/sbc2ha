package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.util.State;
import com.diozero.api.DeviceInterface;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.GpioDeviceFactoryInterface;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Actuator<D extends DeviceInterface, E extends ActuatorEvent<S>, S extends State> implements DeviceInterface {
    protected final D delegate;
    protected final String name;
    protected final Map<ActuatorState, Collection<Consumer<E>>> listeners = new HashMap<>();
    protected final Collection<Consumer<E>> listenersAny = new LinkedHashSet<>();

    public Actuator(D delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    public String getName() {
        return name;
    }


    /**
     * Add a new listener
     *
     * @param listener Callback instance
     */
    public void addListener(Consumer<E> listener, ActuatorState event) {
        if (!listeners.get(event).contains(listener)) {
            listeners.get(event).add(listener);
        }
    }

    /**
     * Remove a specific listener
     *
     * @param listener Callback instance to remove
     */
    public void removeListener(Consumer<E> listener, ActuatorState event) {
        listeners.get(event).remove(listener);

    }

    public void addListenerAny(Consumer<E> listener) {
        if (!listenersAny.contains(listener)) {
            listenersAny.add(listener);
        }
    }

    /**
     * Remove a specific listener
     *
     * @param listener Callback instance to remove
     */
    public void removeListenerAny(Consumer<E> listener) {
        listenersAny.remove(listener);
    }

    public boolean hasListeners() {
        int sum = listeners.values().stream()
                .mapToInt(Collection::size).sum();
        return sum + listenersAny.size() > 0;
    }

    /**
     * Remove all listeners
     */
    public void removeAllListeners() {
        listeners.values().forEach(Collection::clear);
        listenersAny.clear();
    }

    @Override
    public void close() throws RuntimeIOException {
        Logger.trace("close()");
        delegate.close();
        removeAllListeners();
    }

    protected void handleAny(E event) {
        listenersAny.forEach(listener -> listener.accept(event));
    }

    public void whenAny(Consumer<E> consumer) {
        addListenerAny(consumer);
    }

    public enum Direction {
        PRESS,
        RELEASE
    }

    public static abstract class Builder<T extends Builder<T, D>, D> {
        protected D delegate;
        protected String name;
        protected Integer gpio;
        protected PinInfo pinInfo;
        protected boolean activeHigh = true;
        protected Number initialValue = 0;
        protected GpioDeviceFactoryInterface deviceFactory;

        public Builder(int gpio) {
            this.gpio = gpio;
        }

        public Builder(PinInfo pinInfo) {
            this.pinInfo = pinInfo;
        }

        protected abstract T self();

        public T setDeviceFactory(GpioDeviceFactoryInterface deviceFactory) {
            this.deviceFactory = deviceFactory;
            return self();
        }

        public T setName(String name) {
            this.name = name;
            return self();
        }

        public T setGpio(Integer gpio) {
            this.gpio = gpio;
            return self();
        }

        public T setPinInfo(PinInfo pinInfo) {
            this.pinInfo = pinInfo;
            return self();
        }

        public T setDelegate(D delegate) {
            this.delegate = delegate;
            return self();
        }

        public T setActiveHigh(boolean activeHigh) {
            this.activeHigh = activeHigh;
            return self();
        }

        public T setInitialValue(Number initialValue) {
            this.initialValue = initialValue;
            return self();
        }
    }
}
