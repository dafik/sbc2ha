package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.diozero.api.DeviceInterface;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.GpioDeviceFactoryInterface;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public abstract class Actuator implements DeviceInterface {
    protected final String name;
    protected final int id;
    protected final Map<ActuatorState, Collection<Consumer<StateEvent>>> listeners = new HashMap<>();
    protected final Collection<Consumer<StateEvent>> listenersAny = new LinkedHashSet<>();
    private Duration momentaryTurnOff;
    private Duration momentaryTurnOn;
    private Timer timer;

    public Actuator(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    /**
     * Add a new listener
     *
     * @param listener Callback instance
     */
    public void addListener(Consumer<StateEvent> listener, ActuatorState event) {
        if (!listeners.get(event).contains(listener)) {
            listeners.get(event).add(listener);
        }
    }

    /**
     * Remove a specific listener
     *
     * @param listener Callback instance to remove
     */
    public void removeListener(Consumer<StateEvent> listener, ActuatorState event) {
        listeners.get(event).remove(listener);

    }

    public void addListenerAny(Consumer<StateEvent> listener) {
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
        log.trace("close()");
        removeAllListeners();
    }

    protected void handleAny(StateEvent event) {
        listenersAny.forEach(listener -> listener.accept(event));
    }

    public void whenAny(Consumer<StateEvent> consumer) {
        addListenerAny(consumer);
    }

    public void setMomentaryTurnOff(Duration momentaryTurnOff) {
        this.momentaryTurnOff = momentaryTurnOff;
        if (momentaryTurnOff != null) {
            setupTimer();
        }
    }

    public void setMomentaryTurnOn(Duration momentaryTurnOn) {
        this.momentaryTurnOn = momentaryTurnOn;
        if (momentaryTurnOn != null) {
            setupTimer();
        }
    }

    private void setupTimer() {
        if (timer == null) {
            timer = new Timer(name);
        }
    }


    protected void handleMomentaryTurnOn(Runnable callback) {
        if (momentaryTurnOn != null) {
            Scheduler.getInstance().scheduleWithFixedDelay(callback, 0,
                    momentaryTurnOn.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    protected void handleMomentaryTurnOff(Runnable callback) {
        if (momentaryTurnOff != null) {
            Scheduler.getInstance().scheduleWithFixedDelay(callback, 0,
                    momentaryTurnOff.toMillis(), TimeUnit.MILLISECONDS);
        }

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
        protected StateEvent initialState;

        protected Duration momentaryTurnOn;
        protected Duration momentaryTurnOff;

        protected GpioDeviceFactoryInterface deviceFactory;
        protected int id;

        public Builder(int gpio) {
            this.gpio = gpio;
        }

        public Builder(PinInfo pinInfo) {
            this.pinInfo = pinInfo;
        }

        protected Builder() {

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

        public T setInitialState(StateEvent initialState) {
            this.initialState = initialState;
            return self();
        }

        public T setId(int id) {
            this.id = id;
            return self();
        }

        public T setMomentaryTurnOn(Duration momentaryTurnOn) {
            this.momentaryTurnOn = momentaryTurnOn;
            return self();
        }

        public T setMomentaryTurnOff(Duration momentaryTurnOff) {
            this.momentaryTurnOff = momentaryTurnOff;
            return self();
        }
    }
}
