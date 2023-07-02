package com.dfi.sbc2ha.actuator;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.PinInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.Setter;
import org.tinylog.Logger;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.actuator.ActuatorState.OFF;
import static com.dfi.sbc2ha.actuator.ActuatorState.ON;

@Setter
public class Relay extends Actuator<DigitalOutputDevice, RelayEvent, ActuatorState> {

    public Relay(DigitalOutputDevice delegate, String name) {
        super(delegate, name);

        listeners.put(ON, new LinkedHashSet<>());
        listeners.put(OFF, new LinkedHashSet<>());
    }

    private void handleTurnOn(long e) {
        RelayEvent evt = RelayEvent.ofOn(e);
        listeners.get(ON).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }

    private void handleTurnOff(long e) {
        RelayEvent evt = RelayEvent.ofOff(e);
        listeners.get(OFF).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }

    public void whenTurnOn(Consumer<RelayEvent> consumer) {
        addListener(consumer, ON);
    }

    public void whenTurnOff(Consumer<RelayEvent> consumer) {
        addListener(consumer, OFF);
    }

    public void turnOn() {
        Logger.debug("turnOn on: {}", name);
        delegate.on();
        handleTurnOn(System.currentTimeMillis());
    }

    public void turnOff() {
        Logger.debug("turnOff on: {}", name);
        delegate.off();
        handleTurnOff(System.currentTimeMillis());
    }

    public void toggle() {
        Logger.debug("toggle on: {}", name);
        if (delegate.isOn()) {
            turnOff();
        } else {
            turnOn();
        }
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
            return new Relay(delegate, name);
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
                    .setInitialValue((int) initialValue > 0);

            delegate = builder.build();
            return self();
        }
    }
}
