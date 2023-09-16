package com.dfi.sbc2ha.components.actuator;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.actuator.RelayEvent;
import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.services.state.actuator.ActuatorState.OFF;
import static com.dfi.sbc2ha.services.state.actuator.ActuatorState.ON;

@Slf4j
@Setter
public class Relay extends Actuator {

    protected final DigitalOutputDevice delegate;

    public Relay(DigitalOutputDevice delegate, String name, int id) {
        super(name, id);
        this.delegate = delegate;
        listeners.put(ON, new LinkedHashSet<>());
        listeners.put(OFF, new LinkedHashSet<>());
    }

    @Override
    public String getValue() {
        return delegate.isOn() ? "1" : "0";
    }

    private void handleTurnOn(long e) {
        RelayEvent evt = new RelayEvent(ON);
        listeners.get(ON).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }

    private void handleTurnOff(long e) {
        RelayEvent evt = new RelayEvent(OFF);
        listeners.get(OFF).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }

    public void whenTurnOn(Consumer<StateEvent> consumer) {
        addListener(consumer, ON);
    }

    public void whenTurnOff(Consumer<StateEvent> consumer) {
        addListener(consumer, OFF);
    }

    public void turnOn() {
        log.info("turnOn id:{},{}", id, name);
        delegate.on();
        handleMomentaryTurnOn(this::turnOff);
        handleTurnOn(System.currentTimeMillis());
    }


    public void turnOff() {
        log.info("turnOff id:{},{}", id, name);
        delegate.off();
        handleMomentaryTurnOff(this::turnOn);
        handleTurnOff(System.currentTimeMillis());
    }

    public void toggle() {
        log.debug("toggle id:{},{}", id, name);
        if (delegate.isOn()) {
            turnOff();
        } else {
            turnOn();
        }
    }

    @Override
    public void close() throws RuntimeIOException {
        delegate.close();
        super.close();
    }

    @Setter
    public static class Builder extends Actuator.Builder<Builder, DigitalOutputDevice> {

        public Builder(int gpio) {
            super(gpio);
        }

        public Builder(PinInfo pinInfo) {
            super(pinInfo);
        }

        public static Builder builder(int gpio) {
            return new Builder(gpio);
        }

        public static Builder builder(PinInfo pinInfo) {
            return new Builder(pinInfo);
        }


        @Override
        protected Builder self() {
            return this;
        }

        public Relay build() {
            if (delegate == null) {
                setupDelegate();
            }
            Relay relay = new Relay(delegate, name, id);
            relay.setMomentaryTurnOff(momentaryTurnOff);
            relay.setMomentaryTurnOn(momentaryTurnOn);
            return relay;
        }

        protected Builder setupDelegate() {
            if (deviceFactory == null) {
                deviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();
            }
            if (pinInfo == null) {
                pinInfo = deviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(gpio);
            }

            DigitalOutputDevice.Builder builder = DigitalOutputDevice.Builder
                    .builder(pinInfo)
                    .setDeviceFactory(deviceFactory)
                    .setActiveHigh(activeHigh)
                    .setInitialValue(getInitialValue());

            delegate = builder.build();
            return self();
        }

        private boolean getInitialValue() {
            if (initialState == null) {
                return false;
            }
            return ((RelayEvent) initialState).toBoolean();
        }
    }
}

